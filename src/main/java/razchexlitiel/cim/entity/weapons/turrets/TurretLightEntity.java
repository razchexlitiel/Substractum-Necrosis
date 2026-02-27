package razchexlitiel.cim.entity.weapons.turrets;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import razchexlitiel.cim.entity.weapons.bullets.TurretBulletEntity;
import razchexlitiel.cim.item.tags.AmmoRegistry;
import razchexlitiel.cim.sound.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TurretLightEntity extends Monster implements GeoEntity, RangedAttackMob {

    // GeckoLib
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Synched data
    private static final EntityDataAccessor<Boolean> SHOOTING = SynchedEntityData.defineId(TurretLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DEPLOYED = SynchedEntityData.defineId(TurretLightEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(TurretLightEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DEPLOY_TIMER = SynchedEntityData.defineId(TurretLightEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET_ID = SynchedEntityData.defineId(TurretLightEntity.class, EntityDataSerializers.INT);

    // Balance
    private static final int DEPLOY_DURATION = 80;
    private static final int SHOT_ANIMATION_LENGTH = 14;
    private static final double TARGET_LOCK_DISTANCE = 5.0D;
    private static final double CLOSE_COMBAT_RANGE = 5.0D;
    private static final double CLOSE_COMBAT_RANGE_SQR = CLOSE_COMBAT_RANGE * CLOSE_COMBAT_RANGE;

    // Ballistics Constants
    private static final float BULLET_SPEED = 3.0F;
    private static final float BULLET_GRAVITY = 0.01F;
    private static final double DRAG = 0.99;

    // WAR THUNDER STYLE TRACKING
    private Vec3 lastTargetPos = Vec3.ZERO;
    private Vec3 avgTargetVelocity = Vec3.ZERO;
    private Vec3 targetAcceleration = Vec3.ZERO;
    private int trackingTicks = 0;

    // Anti-Recoil Filter
    private long lastShotTimeMs = 0L;
    private long expectedImpactTimeMs = 0L;
    private static final long Y_IMPULSE_FILTER_MS = 350L;

    // Aim tuning
    private static final double AIM_Y_BIAS = 0.05D;

    // Local state
    private int shootAnimTimer = 0;
    private int shotCooldown = 0;
    private int lockSoundCooldown = 0;
    private LivingEntity currentTargetCache = null;
    private int currentTargetPriority = 999;

    // --- OPTIMIZATION VARIABLES ---
    private int raycastSkipTimer = 0;
    private Vec3 cachedSmartTargetPos = null;
    private static final int RAYCAST_INTERVAL = 4; // Check raycasts every 4 ticks
    // ------------------------------

    // Debug
    private Vec3 debugTargetPoint = null;
    private Vec3 debugBallisticVelocity = null;
    private final List<Pair<Vec3, Boolean>> debugScanPoints = new ArrayList<>();

    public Vec3 getDebugTargetPoint() { return debugTargetPoint; }
    public Vec3 getDebugBallisticVelocity() { return debugBallisticVelocity; }
    public List<Pair<Vec3, Boolean>> getDebugScanPoints() { return debugScanPoints; }

    public TurretLightEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SHOOTING, false);
        this.entityData.define(DEPLOYED, false);
        this.entityData.define(OWNER_UUID, Optional.empty());
        this.entityData.define(DEPLOY_TIMER, DEPLOY_DURATION);
        this.entityData.define(TARGET_ID, -1);
    }

    @Override
    public int getMaxHeadYRot() { return 360; }

    @Override
    public int getMaxHeadXRot() { return 80; }

    public Vec3 getMuzzlePos() {
        double offsetY = 10.49 / 16.0;
        double offsetZ = 14.86 / 16.0;

        float yRotRad = this.yHeadRot * ((float) Math.PI / 180F);
        float xRotRad = -this.getXRot() * ((float) Math.PI / 180F);

        double yShift = Math.sin(xRotRad) * offsetZ;
        double forwardShift = Math.cos(xRotRad) * offsetZ;

        double x = this.getX() - Math.sin(yRotRad) * forwardShift;
        double y = this.getY() + offsetY + yShift;
        double z = this.getZ() + Math.cos(yRotRad) * forwardShift;

        return new Vec3(x, y, z);
    }

    // -------------------- Allies Priority (FIXED) --------------------

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (super.isAlliedTo(entity)) return true;

        UUID myOwner = this.getOwnerUUID();
        if (myOwner != null) {
            // 1. Владелец
            if (entity.getUUID().equals(myOwner)) return true;

            // 2. Другая обычная турель с тем же владельцем
            if (entity instanceof TurretLightEntity otherTurret) {
                UUID theirOwner = otherTurret.getOwnerUUID();
                if (myOwner.equals(theirOwner)) return true;
            }

            // 3. Linked-турель с тем же владельцем (ЭТОГО НЕ ХВАТАЛО)
            if (entity instanceof TurretLightLinkedEntity linkedTurret) {
                UUID theirOwner = linkedTurret.getOwnerUUID();
                if (myOwner.equals(theirOwner)) return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------------------

    private int calculateTargetPriority(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || this.isAlliedTo(entity)) return 999;

        double distanceSqr = this.distanceToSqr(entity);
        if (distanceSqr < CLOSE_COMBAT_RANGE_SQR) return 0;

        UUID ownerUUID = this.getOwnerUUID();
        Player owner = ownerUUID != null ? this.level().getPlayerByUUID(ownerUUID) : null;

        if (owner != null) {
            if (owner.getLastHurtByMob() == entity) return 1;
            if (entity instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == owner) return 1;
        }

        List<Entity> nearbyEntities = this.level().getEntities(this, this.getBoundingBox().inflate(16.0D));
        for (Entity e : nearbyEntities) {
            // Проверяем всех союзных турелей (и обычных, и Linked)
            if (e != this && e instanceof LivingEntity livingAlly && this.isAlliedTo(livingAlly)) {
                if (livingAlly.getLastHurtByMob() == entity) return 2;
                if (entity instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == livingAlly) return 2;
            }
        }

        if (this.getLastHurtByMob() == entity) return 3;
        if (owner != null && owner.getLastHurtMob() == entity) return 4;

        if (entity instanceof Monster) return 5;
        if (entity instanceof Player) return 6;

        return 999;
    }

    // -------------------- TICK & Tracking Logic --------------------

    @Override
    public void tick() {
        super.tick();

        this.yBodyRot = this.getYRot();
        this.yBodyRotO = this.getYRot();

        if (!this.level().isClientSide) {
            int currentTimer = this.entityData.get(DEPLOY_TIMER);

            if (currentTimer > 0) {
                this.entityData.set(DEPLOY_TIMER, currentTimer - 1);
            }

            if (currentTimer - 1 <= 0 && !this.isDeployed()) {
                this.entityData.set(DEPLOYED, true);
            }

            if (this.shotCooldown > 0) this.shotCooldown--;
            if (this.lockSoundCooldown > 0) this.lockSoundCooldown--;

            if (this.isShooting()) {
                shootAnimTimer++;
                if (shootAnimTimer >= SHOT_ANIMATION_LENGTH) {
                    this.setShooting(false);
                    shootAnimTimer = 0;
                }
            }

            // Targeting
            LivingEntity target = this.getTarget();
            currentTargetPriority = target != null ? calculateTargetPriority(target) : 999;

            if (target != null && target != currentTargetCache && this.isDeployed()) {
                if (this.lockSoundCooldown <= 0) {
                    if (ModSounds.TURRET_LOCK.isPresent()) {
                        this.playSound(ModSounds.TURRET_LOCK.get(), 1.0F, 1.0F);
                    }
                    this.lockSoundCooldown = 40;
                }
                currentTargetCache = target;

                // Reset tracker on new target
                this.trackingTicks = 0;
                this.lastTargetPos = target.position();
                this.avgTargetVelocity = Vec3.ZERO;
                this.targetAcceleration = Vec3.ZERO;
                this.cachedSmartTargetPos = null;

            } else if (target == null) {
                currentTargetCache = null;
                this.cachedSmartTargetPos = null;
            }

            // WAR THUNDER TRACKER UPDATE
            if (target != null && target.isAlive()) {
                Vec3 currentPos = target.position();

                if (trackingTicks > 0) {
                    Vec3 instantaneousVel = currentPos.subtract(lastTargetPos);
                    this.avgTargetVelocity = this.avgTargetVelocity.lerp(instantaneousVel, 0.15);
                    Vec3 newAccel = instantaneousVel.subtract(this.avgTargetVelocity);
                    this.targetAcceleration = this.targetAcceleration.lerp(newAccel, 0.05);
                } else {
                    this.avgTargetVelocity = target.getDeltaMovement();
                    this.targetAcceleration = Vec3.ZERO;
                }
                this.lastTargetPos = currentPos;
                this.trackingTicks++;
            } else {
                this.trackingTicks = 0;
                this.avgTargetVelocity = Vec3.ZERO;
                this.targetAcceleration = Vec3.ZERO;
                this.debugTargetPoint = null;
                this.debugBallisticVelocity = null;
            }

            // Sync target for debug
            int targetId = target != null ? target.getId() : -1;
            if (this.entityData.get(TARGET_ID) != targetId) {
                this.entityData.set(TARGET_ID, targetId);
            }

            if (target != null && this.isDeployed()) {
                Vec3 aimPos = getAimTargetPosition(target);
                if (aimPos != null) {
                    this.getLookControl().setLookAt(aimPos.x, aimPos.y, aimPos.z, 30.0F, 30.0F);
                }
            }

            // Target Switching Logic
            if (this.tickCount % 10 == 0) {
                LivingEntity closeThreat = findClosestThreat();
                if (closeThreat != null && closeThreat != this.getTarget()) {
                    int newPriority = calculateTargetPriority(closeThreat);
                    if (newPriority < currentTargetPriority) {
                        this.setTarget(closeThreat);
                        currentTargetPriority = newPriority;
                    }
                }

                boolean canSwitchToFarTarget = true;
                if (target != null && target.isAlive()) {
                    if (this.distanceToSqr(target) < TARGET_LOCK_DISTANCE * TARGET_LOCK_DISTANCE) {
                        canSwitchToFarTarget = false;
                    }
                }

                if (canSwitchToFarTarget) {
                    UUID ownerUUID = this.getOwnerUUID();
                    if (ownerUUID != null) {
                        Player owner = this.level().getPlayerByUUID(ownerUUID);
                        if (owner != null) {
                            LivingEntity ownerAttacker = owner.getLastHurtByMob();
                            if (ownerAttacker != null && ownerAttacker != this.getTarget() && ownerAttacker.isAlive() && !isAlliedTo(ownerAttacker)) {
                                int newPriority = calculateTargetPriority(ownerAttacker);
                                if (newPriority < currentTargetPriority) {
                                    this.setTarget(ownerAttacker);
                                    currentTargetPriority = newPriority;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Client side debug calc
            int targetId = this.entityData.get(TARGET_ID);
            if (targetId != -1) {
                Entity targetEntity = this.level().getEntity(targetId);
                if (targetEntity instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                    Vec3 muzzle = getMuzzlePos();
                    this.debugBallisticVelocity = calculateBallisticVelocity(livingTarget, muzzle, BULLET_SPEED, BULLET_GRAVITY);
                } else {
                    this.debugTargetPoint = null;
                    this.debugBallisticVelocity = null;
                }
            } else {
                this.debugTargetPoint = null;
                this.debugBallisticVelocity = null;
            }
        }
    }

    private Vec3 getAimTargetPosition(LivingEntity target) {
        Vec3 muzzle = getMuzzlePos();
        Vec3 velocity = calculateBallisticVelocity(target, muzzle, BULLET_SPEED, BULLET_GRAVITY);
        if (velocity != null) {
            return muzzle.add(velocity.normalize().scale(10.0));
        } else {
            Vec3 smart = getSmartTargetPos(target);
            return smart != null ? smart : null;
        }
    }

    // -------------------- BALLISTICS WT Style --------------------

    private Vec3 calculateBallisticVelocity(LivingEntity target, Vec3 muzzlePos, float speed, float gravity) {
        Vec3 visibleBasePos = getSmartTargetPos(target);
        if (visibleBasePos == null) return null;

        double maxVisibleY = visibleBasePos.y + 0.5;
        Vec3 targetVel;
        Vec3 targetAcc;

        if (trackingTicks > 5) {
            targetVel = this.avgTargetVelocity;

            long now = System.currentTimeMillis();
            boolean postImpact = now < expectedImpactTimeMs;
            boolean insideFilterWindow = now < expectedImpactTimeMs + Y_IMPULSE_FILTER_MS;
            boolean isRecoilState = postImpact || insideFilterWindow || target.hurtTime > 0;

            if (target.onGround() || isRecoilState) {
                targetVel = new Vec3(targetVel.x, 0, targetVel.z);
                targetAcc = new Vec3(this.targetAcceleration.x, 0, this.targetAcceleration.z);
            } else {
                targetAcc = this.targetAcceleration;
            }
        } else {
            targetVel = target.getDeltaMovement();
            if (target.onGround()) {
                targetVel = new Vec3(targetVel.x, 0, targetVel.z);
            }
            targetAcc = Vec3.ZERO;
        }

        double dist = visibleBasePos.distanceTo(muzzlePos);
        double t = calculateFlightTime(dist, speed);
        Vec3 predictedPos = visibleBasePos;

        for (int i = 0; i < 4; i++) {
            Vec3 velocityPart = targetVel.scale(t);
            Vec3 accelPart = targetAcc.scale(0.5 * t * t);
            predictedPos = visibleBasePos.add(velocityPart).add(accelPart);

            if (predictedPos.y > maxVisibleY) {
                predictedPos = new Vec3(predictedPos.x, maxVisibleY, predictedPos.z);
            }
        }

        if (!canSeePoint(muzzlePos, predictedPos)) {
            predictedPos = visibleBasePos;
        }

        double newDist = predictedPos.distanceTo(muzzlePos);
        double newT = calculateFlightTime(newDist, speed);
        if (Math.abs(newT - t) < 0.05) {
            t = newT;
        }

        this.debugTargetPoint = predictedPos;

        double dragFactor = getDragCompensationFactor(t);
        double dirX = predictedPos.x - muzzlePos.x;
        double dirZ = predictedPos.z - muzzlePos.z;
        double dirY = predictedPos.y - muzzlePos.y;
        double horizontalDist = Math.sqrt(dirX * dirX + dirZ * dirZ) * dragFactor;
        double v = speed;
        double v2 = v * v;
        double v4 = v2 * v2;
        double g = gravity * dragFactor;

        double discriminant = v4 - g * (g * horizontalDist * horizontalDist + 2 * dirY * v2);
        if (discriminant < 0) return null;

        double sqrtDisc = Math.sqrt(discriminant);
        double tanTheta = (v2 - sqrtDisc) / (g * horizontalDist);
        double pitch = Math.atan(tanTheta);
        double yaw = Math.atan2(dirZ, dirX);

        double groundSpeed = v * Math.cos(pitch);
        double vy = v * Math.sin(pitch);

        return new Vec3(groundSpeed * Math.cos(yaw), vy, groundSpeed * Math.sin(yaw));
    }

    private double calculateFlightTime(double dist, double speed) {
        double term = 1.0 - (dist * (1.0 - DRAG)) / speed;
        if (term <= 0.05) return 60.0;
        return Math.log(term) / Math.log(DRAG);
    }

    private double getDragCompensationFactor(double t) {
        if (t < 0.001) return 1.0;
        double numerator = t * (1.0 - DRAG);
        double denominator = 1.0 - Math.pow(DRAG, t);
        if (denominator < 0.001) return 1.0;
        return numerator / denominator;
    }

    // -------------------- Smart Hitbox (OPTIMIZED) --------------------

    private Vec3 getSmartTargetPos(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            if (level().isClientSide) debugScanPoints.clear();
            return null;
        }

        if (this.raycastSkipTimer > 0 && this.cachedSmartTargetPos != null) {
            this.raycastSkipTimer--;
            if (target.distanceToSqr(this.cachedSmartTargetPos) < 4.0) {
                return this.cachedSmartTargetPos;
            }
        }

        if (level().isClientSide) debugScanPoints.clear();

        Vec3 start = this.getMuzzlePos();
        Vec3 eyePos = target.getEyePosition();

        if (canSeePoint(start, eyePos)) {
            if (this.level().isClientSide) {
                this.debugScanPoints.add(Pair.of(eyePos, true));
            }
            updateSmartCache(eyePos);
            return eyePos;
        } else {
            if (this.level().isClientSide) {
                this.debugScanPoints.add(Pair.of(eyePos, false));
            }
        }

        AABB aabb = target.getBoundingBox();
        List<Vec3> visiblePoints = new ArrayList<>();

        int stepsX = 2;
        int stepsY = 3;
        int stepsZ = 2;

        for (int y = stepsY; y >= 0; y--) {
            for (int x = 0; x <= stepsX; x++) {
                for (int z = 0; z <= stepsZ; z++) {
                    boolean isOuterX = x == 0 || x == stepsX;
                    boolean isOuterY = y == 0 || y == stepsY;
                    boolean isOuterZ = z == 0 || z == stepsZ;
                    if (!isOuterX && !isOuterZ && !isOuterY) continue;

                    double lx = (double)x / stepsX;
                    double ly = (double)y / stepsY;
                    double lz = (double)z / stepsZ;

                    Vec3 point = new Vec3(
                            aabb.minX + (aabb.maxX - aabb.minX) * lx,
                            aabb.minY + (aabb.maxY - aabb.minY) * ly,
                            aabb.minZ + (aabb.maxZ - aabb.minZ) * lz
                    );

                    boolean visible = canSeePoint(start, point);
                    if (this.level().isClientSide) {
                        this.debugScanPoints.add(Pair.of(point, visible));
                    }

                    if (visible) {
                        visiblePoints.add(point);
                        if (ly > 0.7) {
                            updateSmartCache(point);
                            return point;
                        }
                    }
                }
            }
        }

        if (visiblePoints.isEmpty()) {
            double cx = aabb.minX + aabb.getXsize() * 0.5;
            double cz = aabb.minZ + aabb.getZsize() * 0.5;
            int centerSteps = 3;
            for (int i = 0; i <= centerSteps; i++) {
                double ly = (double)i / centerSteps;
                Vec3 point = new Vec3(cx, aabb.minY + (aabb.maxY - aabb.minY) * ly, cz);
                boolean visible = canSeePoint(start, point);
                if (this.level().isClientSide) {
                    this.debugScanPoints.add(Pair.of(point, visible));
                }
                if (visible) {
                    visiblePoints.add(point);
                }
            }
        }

        if (visiblePoints.isEmpty()) {
            this.raycastSkipTimer = 0;
            return null;
        }

        visiblePoints.sort((p1, p2) -> Double.compare(p2.y, p1.y));
        Vec3 best = visiblePoints.get(0);
        updateSmartCache(best);
        return best;
    }

    private void updateSmartCache(Vec3 pos) {
        this.cachedSmartTargetPos = pos;
        this.raycastSkipTimer = RAYCAST_INTERVAL;
    }

    private boolean canSeePoint(Vec3 start, Vec3 end) {
        BlockHitResult blockHit = this.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return blockHit.getType() == HitResult.Type.MISS || start.distanceToSqr(blockHit.getLocation()) >= start.distanceToSqr(end) - 0.5;
    }

    private boolean isLineOfFireSafe(LivingEntity target) {
        Vec3 start = this.getMuzzlePos();
        Vec3 end = getSmartTargetPos(target);
        if (end == null) return false;

        Vec3 vec3 = end.subtract(start);
        double distance = vec3.length();
        vec3 = vec3.normalize();

        double stepSize = 0.5;
        for (double d = 0; d < distance; d += stepSize) {
            Vec3 checkPos = start.add(vec3.scale(d));
            AABB checkBox = new AABB(checkPos.subtract(0.5, 0.5, 0.5), checkPos.add(0.5, 0.5, 0.5));
            List<Entity> list = this.level().getEntities(this, checkBox);
            for (Entity e : list) {
                if (e != this && e != target && e instanceof LivingEntity living) {
                    if (this.isAlliedTo(living)) return false;
                }
            }
        }
        return true;
    }

    private boolean canShootSafe(LivingEntity target) {
        return getSmartTargetPos(target) != null && isLineOfFireSafe(target);
    }

    private LivingEntity findClosestThreat() {
        LivingEntity closest = null;
        double closestDist = CLOSE_COMBAT_RANGE_SQR;
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(CLOSE_COMBAT_RANGE));
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living && !this.isAlliedTo(living) && living.isAlive()) {
                double dist = this.distanceToSqr(living);
                if (dist < closestDist) {
                    closest = living;
                    closestDist = dist;
                }
            }
        }
        return closest;
    }

    // -------------------- Fire! --------------------

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        if (!this.isDeployed()) return;
        if (this.shotCooldown > 0) return;
        if (!canShootSafe(target)) return;

        Vec3 muzzlePos = getMuzzlePos();
        Vec3 ballisticVelocity = calculateBallisticVelocity(target, muzzlePos, BULLET_SPEED, BULLET_GRAVITY);
        if (ballisticVelocity == null) return;

        double horizontalDist = Math.sqrt(ballisticVelocity.x * ballisticVelocity.x + ballisticVelocity.z * ballisticVelocity.z);

        // 1. УГОЛ ДЛЯ ПРОВЕРКИ ТУРЕЛИ (Оставляем как было в оригинале!)
        // Турель использует систему координат с offset -90
        float turretCheckYaw = (float) (Math.atan2(ballisticVelocity.z, ballisticVelocity.x) * (180D / Math.PI)) - 90.0F;
        float targetPitch = (float) (Math.atan2(ballisticVelocity.y, horizontalDist) * (180D / Math.PI));

        float currentYaw = this.yHeadRot;
        float currentPitch = -this.getXRot();

        // Сравниваем именно с turretCheckYaw
        float yawDiff = Math.abs(wrapDegrees(turretCheckYaw - currentYaw));
        float pitchDiff = Math.abs(wrapDegrees(targetPitch - currentPitch));

        if (yawDiff > 10.0F || pitchDiff > 10.0F) return;

        this.lastShotTimeMs = System.currentTimeMillis();
        double dist = muzzlePos.distanceTo(target.position());
        long flightTimeMs = (long) ((dist / BULLET_SPEED) * 50.0);
        this.expectedImpactTimeMs = this.lastShotTimeMs + flightTimeMs;

        this.shotCooldown = SHOT_ANIMATION_LENGTH;
        this.setShooting(true);
        this.shootAnimTimer = 0;

        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            TurretBulletEntity bullet = new TurretBulletEntity(serverLevel, this);

            AmmoRegistry.AmmoType randomAmmo = AmmoRegistry.getRandomAmmoForCaliber("20mm_turret", this.level().random);
            if (randomAmmo != null) {
                bullet.setAmmoType(randomAmmo);
            } else {
                bullet.setAmmoType(new AmmoRegistry.AmmoType("default", "20mm_turret", 6.0f, 3.0f, false));
            }

            bullet.setPos(muzzlePos.x, muzzlePos.y, muzzlePos.z);
            bullet.setDeltaMovement(ballisticVelocity);

            // 2. УГОЛ ДЛЯ ПУЛИ (Исправленная формула для визуализации)
            // Пуля использует atan2(x, z) как в методе alignToVelocity
            float bulletYaw = (float) (Math.atan2(ballisticVelocity.x, ballisticVelocity.z) * (180D / Math.PI));

            bullet.setYRot(bulletYaw);
            bullet.setXRot(targetPitch);

            // Интерполяция (чтобы не дергалась)
            bullet.yRotO = bulletYaw;
            bullet.xRotO = targetPitch;

            serverLevel.addFreshEntity(bullet);

            if (ModSounds.TURRET_FIRE.isPresent()) {
                this.playSound(ModSounds.TURRET_FIRE.get(), 1.0F, 1.0F);
            }
        }
    }


    private float wrapDegrees(float degrees) {
        float f = degrees % 360.0F;
        if (f >= 180.0F) f -= 360.0F;
        if (f < -180.0F) f += 360.0F;
        return f;
    }

    // -------------------- GeckoLib & NBT --------------------

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "deploy_ctrl", 0, event -> {
            if (!this.isDeployed()) return event.setAndContinue(RawAnimation.begin().thenPlay("deploy"));
            return PlayState.STOP;
        }));

        controllers.add(new AnimationController<>(this, "shoot_ctrl", 0, event -> {
            if (this.isShooting()) {
                if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("shot"));
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUUID() != null) tag.putUUID("Owner", this.getOwnerUUID());
        tag.putBoolean("Deployed", this.isDeployed());
        tag.putInt("DeployTimer", this.entityData.get(DEPLOY_TIMER));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) this.entityData.set(OWNER_UUID, Optional.of(tag.getUUID("Owner")));
        if (tag.contains("Deployed")) this.entityData.set(DEPLOYED, tag.getBoolean("Deployed"));
        if (tag.contains("DeployTimer")) this.entityData.set(DEPLOY_TIMER, tag.getInt("DeployTimer"));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.Goal() {
            private final TurretLightEntity turret = TurretLightEntity.this;

            @Override
            public boolean canUse() {
                LivingEntity target = this.turret.getTarget();
                return this.turret.isDeployed() && target != null && target.isAlive() && this.turret.distanceToSqr(target) < 1225.0D; // 35^2
            }

            @Override
            public void start() {
                this.turret.getNavigation().stop();
            }

            @Override
            public void stop() {
                this.turret.setShooting(false);
            }

            @Override
            public boolean requiresUpdateEveryTick() { return true; }

            @Override
            public void tick() {
                LivingEntity target = this.turret.getTarget();
                if (target == null) return;
                this.turret.getSensing().tick();
                this.turret.performRangedAttack(target, 1.0F);
            }
        });

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> {
            if (this.isAlliedTo(entity)) return false;
            UUID ownerUUID = this.getOwnerUUID();
            if (ownerUUID != null) {
                Player owner = this.level().getPlayerByUUID(ownerUUID);
                if (owner != null) {
                    if (owner.getLastHurtByMob() == entity) return true;
                    if (entity instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == owner) return true;
                }
            }
            return false;
        }));

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> {
            if (this.isAlliedTo(entity)) return false;
            // Scan for turret allies nearby
            List<Entity> allies = this.level().getEntities(this, this.getBoundingBox().inflate(16.0D));
            for (Entity e : allies) {
                if (e != this && e instanceof LivingEntity ally && this.isAlliedTo(ally)) {
                    if (ally.getLastHurtByMob() == entity) return true;
                    if (entity instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == ally) return true;
                }
            }
            return false;
        }));

        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers(TurretLightEntity.class));

        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> {
            if (this.isAlliedTo(entity)) return false;
            UUID ownerUUID = this.getOwnerUUID();
            if (ownerUUID != null) {
                Player owner = this.level().getPlayerByUUID(ownerUUID);
                return owner != null && owner.getLastHurtMob() == entity;
            }
            return false;
        }));

        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, entity -> !this.isAlliedTo(entity) && TurretLightEntity.this.isDeployed()));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, entity -> !this.isAlliedTo(entity) && TurretLightEntity.this.isDeployed()));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    public void setOwner(Player player) {
        this.entityData.set(OWNER_UUID, Optional.of(player.getUUID()));
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public void setShooting(boolean shooting) {
        this.entityData.set(SHOOTING, shooting);
    }

    public boolean isShooting() {
        return this.entityData.get(SHOOTING);
    }

    public boolean isDeployed() {
        return this.entityData.get(DEPLOYED);
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public double getBoneResetTime() { return 0; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {}

    @Override
    public boolean hasLineOfSight(Entity entity) {
        if (entity instanceof LivingEntity living) {
            return this.canShootSafe(living);
        }
        return super.hasLineOfSight(entity);
    }

    public void setOwnerUUIDDirect(UUID uuid) {
        this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
    }
}
