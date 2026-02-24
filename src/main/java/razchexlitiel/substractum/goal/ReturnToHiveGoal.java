package razchexlitiel.substractum.goal;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.BlockEntity;
import razchexlitiel.substractum.api.hive.HiveNetworkManager;
import razchexlitiel.substractum.api.hive.HiveNetworkMember;
import razchexlitiel.substractum.entity.mobs.DepthWormEntity;

import java.util.EnumSet;

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
        if (worm.getTarget() != null || worm.tickCount < nextSearchTick) return false;

        this.targetPos = findNearestEntry();
        return this.targetPos != null;
    }

    private BlockPos findNearestEntry() {
        BlockPos wormPos = worm.blockPosition();
        HiveNetworkManager manager = HiveNetworkManager.get(worm.level());
        if (manager == null) return null;

        BlockPos bestPos = null;
        double minCheckDist = Double.MAX_VALUE;

        // Оптимальный радиус для поиска хода
        int radius = 12;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos p = wormPos.offset(x, y, z);

                    // Сначала быстрая проверка дистанции (квадрат), чтобы не дергать BlockEntity лишний раз
                    double d = worm.distanceToSqr(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);

                    // Если этот блок уже дальше, чем тот, что мы нашли — скипаем
                    if (d >= minCheckDist) continue;

                    BlockEntity be = worm.level().getBlockEntity(p);
                    if (be instanceof HiveNetworkMember member && member.getNetworkId() != null) {
                        // Если блок принадлежит сети и там есть места
                        if (manager.hasFreeNest(member.getNetworkId(), worm.level())) {
                            minCheckDist = d;
                            bestPos = p.immutable();
                        }
                    }
                }
            }
        }
        return bestPos;
    }

    private void sendDebug(String msg) {
        if (!worm.level().isClientSide) {
            worm.level().players().forEach(p -> p.sendSystemMessage(Component.literal(msg)));
        }
    }

    private void spawnDebugParticles(BlockPos p) {
        if (!worm.level().isClientSide) {
            ((net.minecraft.server.level.ServerLevel)worm.level()).sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    p.getX() + 0.5, p.getY() + 1.2, p.getZ() + 0.5, 3, 0.1, 0.1, 0.1, 0.01
            );
        }
    }

    @Override
    public void start() {
        // Сообщаем в консоль/лог, что червь нашел дом
        // System.out.println("Червь нашел вход на " + targetPos);
    }

    @Override
    public void tick() {
        if (targetPos == null) return;

        worm.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, 1.0D);

        if (worm.distanceToSqr(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5) < 2.5D) {
            HiveNetworkManager manager = HiveNetworkManager.get(worm.level());
            BlockEntity be = worm.level().getBlockEntity(targetPos);

            if (be instanceof HiveNetworkMember member && member.getNetworkId() != null) {
                // ТОЛЬКО ПРИ КАСАНИИ проверяем, есть ли куда телепортировать червя
                if (manager.hasFreeNest(member.getNetworkId(), worm.level())) {
                    CompoundTag tag = new CompoundTag();
                    worm.saveWithoutId(tag);
                    if (manager.addWormToNetwork(member.getNetworkId(), tag, targetPos, worm.level())) {
                        worm.discard(); // Успех!
                    }
                } else {
                    // Если мест нет, червь "понимает" это только уткнувшись носом
                    this.nextSearchTick = worm.tickCount + 200;
                    this.targetPos = null;
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetPos != null && worm.getTarget() == null;
    }
}