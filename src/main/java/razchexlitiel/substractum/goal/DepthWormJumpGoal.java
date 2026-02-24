package razchexlitiel.substractum.goal;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import razchexlitiel.substractum.entity.mobs.DepthWormEntity;

import java.util.EnumSet;

public class DepthWormJumpGoal extends Goal {
    private final DepthWormEntity worm;
    private LivingEntity target;
    private final double speedModifier;
    private final float jumpRangeMin, jumpRangeMax;
    private int jumpTimer;
    private final int PREPARE_TIME = 30; // 1.5 сек

    public DepthWormJumpGoal(DepthWormEntity worm, double speedModifier, float jumpRangeMin, float jumpRangeMax) {
        this.worm = worm;
        this.speedModifier = speedModifier;
        this.jumpRangeMin = jumpRangeMin;
        this.jumpRangeMax = jumpRangeMax;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = this.worm.getTarget();
        if (this.target == null || !this.target.isAlive()) return false;
        double dist = this.worm.distanceTo(this.target);
        return dist >= this.jumpRangeMin && dist <= this.jumpRangeMax;
    }

    @Override
    public boolean canContinueToUse() { return this.canUse() || this.jumpTimer > 0; }

    @Override
    public void start() {
        this.jumpTimer = 30; // 1.5 сек
        this.worm.setAttacking(true); // Открывает рот
        this.worm.getNavigation().stop();
        // Принудительно посылаем пакет на клиент, чтобы обновить флаги
        this.worm.hasImpulse = true;
    }




    @Override
    public void stop() {
        this.target = null;
        this.worm.setAttacking(false);
    }

    @Override
    public void tick() {
        if (this.target == null || !this.target.isAlive()) {
            this.worm.setAttacking(false);
            return;
        }
        double dist = this.worm.distanceTo(this.target);

        if (dist > this.jumpRangeMax + 2.0F) {
            this.worm.setAttacking(false);
            this.jumpTimer = 0;
            return;
        }

        this.worm.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        if (--this.jumpTimer <= 0) {
            doJump();
            // ВНИМАНИЕ: setAttacking(false) ТЕПЕРЬ УДАЛЕН ОТСЮДА!
            // Он сработает либо в stop(), либо при ударе в классе Entity.
            this.worm.ignoreFallDamageTicks = 30;
        }
    }

    private void doJump() {
        Vec3 targetPos = this.target.position().add(0, this.target.getBbHeight() * 0.5, 0);
        Vec3 jumpVector = targetPos.subtract(this.worm.position());
        double horizontalDist = Math.sqrt(jumpVector.x * jumpVector.x + jumpVector.z * jumpVector.z);
        double speed = 0.8 + (horizontalDist * 0.1);
        Vec3 velocity = jumpVector.normalize().scale(speed);
        double verticalBoost = 0.15 + (horizontalDist * 0.04);
        this.worm.setDeltaMovement(velocity.x, verticalBoost, velocity.z);
        this.worm.setFlying(true);
        this.worm.setAttacking(false);   // <-- добавить
    }



}