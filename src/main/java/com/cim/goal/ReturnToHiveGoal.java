package com.cim.goal;

import com.cim.api.hive.HiveNetwork;
import com.cim.api.hive.HiveNetworkManager;
import com.cim.api.hive.HiveNetworkMember;
import com.cim.block.entity.hive.DepthWormNestBlockEntity;
import com.cim.entity.mobs.DepthWormEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.EnumSet;
import java.util.UUID;

public class ReturnToHiveGoal extends Goal {
    private final DepthWormEntity worm;
    private BlockPos targetPos;
    private int nextSearchTick;

    public ReturnToHiveGoal(DepthWormEntity worm) {
        this.worm = worm;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Не идем домой, если есть живая цель
        LivingEntity target = worm.getTarget();
        if (target != null && target.isAlive()) return false;

        // ОПТИМИЗАЦИЯ: Ищем дом не каждый тик
        if (worm.tickCount < nextSearchTick) return false;
        nextSearchTick = worm.tickCount + 10 + worm.getRandom().nextInt(10);

        // 1. Проверяем "запомненный" дом
        BlockPos home = worm.getHomePos();
        if (home != null) {
            if (isValidNest(home)) {
                this.targetPos = home;
                return true;
            }
            worm.setHomePos(null);
        }

        // 2. Ищем новое ближайшее гнездо
        this.targetPos = findNearestEntry();
        if (this.targetPos != null) {
            worm.setHomePos(this.targetPos);
            return true;
        }
        return false;
    }

    private boolean isValidNest(BlockPos pos) {
        BlockEntity be = worm.level().getBlockEntity(pos);
        if (be instanceof DepthWormNestBlockEntity nest) {
            HiveNetworkManager manager = HiveNetworkManager.get(worm.level());
            return !nest.isFull() && nest.getNetworkId() != null;
        }
        return false;
    }

    private BlockPos findNearestEntry() {
        BlockPos wormPos = worm.blockPosition();
        HiveNetworkManager manager = HiveNetworkManager.get(worm.level());
        if (manager == null) return null;

        BlockPos bestNest = null;
        double bestDist = Double.MAX_VALUE;
        int radius = 16; // Чуть увеличил радиус для комфорта

        // Оптимизированный поиск только Гнёзд
        for (int x = -radius; x <= radius; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos p = wormPos.offset(x, y, z);
                    BlockEntity be = worm.level().getBlockEntity(p);

                    if (be instanceof DepthWormNestBlockEntity nest) {
                        if (!nest.isFull()) {
                            double d = worm.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                            if (d < bestDist) {
                                bestDist = d;
                                bestNest = p.immutable();
                            }
                        }
                    }
                }
            }
        }
        return bestNest;
    }

    @Override
    public void tick() {
        if (targetPos == null) return;

        double targetX = targetPos.getX() + 0.5;
        double targetY = targetPos.getY();
        double targetZ = targetPos.getZ() + 0.5;

        worm.getNavigation().moveTo(targetX, targetY, targetZ, 1.2D);
        worm.getLookControl().setLookAt(targetX, targetY + 0.5, targetZ);

        double distSq = worm.distanceToSqr(targetX, targetY, targetZ);

        // ВХОД В УЛЕЙ
        if (distSq < 2.5D) {
            // "Всасывание" червя в центр блока для красоты
            worm.setDeltaMovement(worm.getDeltaMovement().add(
                    (targetX - worm.getX()) * 0.2,
                    (targetY - worm.getY()) * 0.2,
                    (targetZ - worm.getZ()) * 0.2
            ));

            HiveNetworkManager manager = HiveNetworkManager.get(worm.level());
            BlockEntity be = worm.level().getBlockEntity(targetPos);

            if (be instanceof DepthWormNestBlockEntity nest) {
                UUID netId = nest.getNetworkId();
                if (netId != null) {
                    HiveNetwork network = manager.getNetwork(netId);

                    // Начисление очков
                    int kills = worm.getKills();
                    network.killsPool = Math.min(50, network.killsPool + kills);

                    System.out.println("[Hive] Червь вошел. Очков в сети: " + network.killsPool);

                    CompoundTag tag = new CompoundTag();
                    worm.saveWithoutId(tag);
                    tag.putInt("Kills", 0); // Чистим перед сохранением в BE

                    if (manager.addWormToNetwork(netId, tag, targetPos, worm.level())) {
                        worm.discard();
                    }
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetPos != null && isValidNest(targetPos) && worm.getTarget() == null;
    }

    @Override
    public void stop() {
        this.targetPos = null;
        worm.getNavigation().stop();
    }
}