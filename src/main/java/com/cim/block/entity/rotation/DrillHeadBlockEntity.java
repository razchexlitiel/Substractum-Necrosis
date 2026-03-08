package com.cim.block.entity.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import com.cim.api.rotation.RotationNetworkHelper;
import com.cim.api.rotation.RotationSource;
import com.cim.api.rotation.RotationalNode;
import com.cim.block.basic.rotation.DrillHeadBlock;
import com.cim.block.entity.ModBlockEntities;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DrillHeadBlockEntity extends BlockEntity implements GeoBlockEntity, RotationalNode {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private long speed = 0;
    private long torque = 0;

    private long lastBreakTick = 0;
    private static final int BREAK_COOLDOWN = 20; // тиков между блоками (1 секунда)
    private RotationSource cachedSource;
    private long cacheTimestamp;
    private static final long CACHE_LIFETIME = 10;

    private float currentAnimationSpeed = 0f;
    private static final float ACCELERATION = 0.1f;
    private static final float DECELERATION = 0.03f;
    private static final int STOP_DELAY_TICKS = 5;
    private static final float MIN_ANIM_SPEED = 0.005f;
    private int ticksWithoutPower = 0;
    private static final RawAnimation ROTATION = RawAnimation.begin().thenLoop("rotation");

    @Nullable
    private BlockPos placerPos; // позиция разместителя, который обслуживает эту головку

    public DrillHeadBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRILL_HEAD_BE.get(), pos, state);
    }

    public void setPlacerPos(BlockPos pos) {
        this.placerPos = pos;
        setChanged();
        sync();
    }

    @Nullable
    public BlockPos getPlacerPos() { return placerPos; }

    // Rotational
    @Override public long getSpeed() { return speed; }
    @Override public long getTorque() { return torque; }
    @Override public long getMaxSpeed() { return 0; }
    @Override public long getMaxTorque() { return 0; }

    // RotationalNode
    @Override @Nullable public RotationSource getCachedSource() { return cachedSource; }
    @Override public void setCachedSource(@Nullable RotationSource source, long gameTime) {
        this.cachedSource = source;
        this.cacheTimestamp = gameTime;
    }
    @Override public boolean isCacheValid(long currentTime) {
        return cachedSource != null && (currentTime - cacheTimestamp) <= CACHE_LIFETIME;
    }
    @Override public void invalidateCache() {
        if (this.cachedSource != null) {
            this.cachedSource = null;
            if (level != null && !level.isClientSide) {
                invalidateNeighborCaches();
            }
        }
    }
    private void invalidateNeighborCaches() {
        if (level == null || level.isClientSide) return;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(dir);
            if (level.getBlockEntity(neighborPos) instanceof RotationalNode node) {
                node.invalidateCache();
            }
        }
    }
    @Override
    public Direction[] getPropagationDirections(@Nullable Direction fromDir) {
        Direction myFacing = getBlockState().getValue(DrillHeadBlock.FACING);
        if (fromDir != null) {
            if (fromDir == myFacing || fromDir == myFacing.getOpposite()) {
                return new Direction[]{fromDir.getOpposite()};
            } else {
                return new Direction[0];
            }
        } else {
            return new Direction[]{myFacing, myFacing.getOpposite()};
        }
    }

    // Тик
    public static void tick(Level level, BlockPos pos, BlockState state, DrillHeadBlockEntity be) {
        if (level.isClientSide) {
            be.handleClientAnimation();
            return;
        }

        // 1. Обновляем энергию вращения
        long currentTime = level.getGameTime();
        if (!be.isCacheValid(currentTime)) {
            RotationSource source = RotationNetworkHelper.findSource(be, null);
            be.setCachedSource(source, currentTime);
        }

        RotationSource src = be.getCachedSource();
        be.speed = (src != null) ? src.speed() : 0;
        be.torque = (src != null) ? src.torque() : 0;

        // 2. Бурение: только если есть скорость и прошел кулдаун
        if (be.speed > 0 && level.getGameTime() - be.lastBreakTick >= BREAK_COOLDOWN) {
            if (be.tryBreakBlock(level, pos, state)) {
                be.lastBreakTick = level.getGameTime();
                // 3. Если блок сломан и есть разместитель — двигаемся вперед
                if (be.placerPos != null && level.getBlockEntity(be.placerPos) instanceof ShaftPlacerBlockEntity) {
                    be.moveForward(level, pos, state);
                }
            }
        }
    }

    private List<BlockPos> getBlocksToBreak(BlockPos pos, Direction facing) {
        List<BlockPos> targets = new ArrayList<>();
        // Базовый блок перед головкой
        BlockPos front = pos.relative(facing);

        // Определяем две оси, перпендикулярные направлению
        Direction.Axis axis = facing.getAxis();
        Direction[] horizontals;
        if (axis == Direction.Axis.X) {
            horizontals = new Direction[]{Direction.NORTH, Direction.SOUTH};
        } else if (axis == Direction.Axis.Z) {
            horizontals = new Direction[]{Direction.WEST, Direction.EAST};
        } else { // вертикальное направление (вверх/вниз) – тогда ширина и высота задаются по горизонтали
            horizontals = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        }

        // Для горизонтальных направлений: высота = Y, Y+1; ширина = 3 блока по горизонтали
        if (axis.isHorizontal()) {
            for (int yOffset = 0; yOffset <= 1; yOffset++) {
                for (Direction horizontal : horizontals) {
                    targets.add(front.relative(horizontal, -1).above(yOffset));
                    targets.add(front.above(yOffset));
                    targets.add(front.relative(horizontal, 1).above(yOffset));
                }
            }
        } else { // вертикальное направление – тогда "ширина" и "высота" меняются ролями
            // Например, при движении вверх/вниз копаем область 3x2 по горизонтали
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    // Только 2 блока в высоту? Но здесь высота – это ось Y, а мы уже движемся по Y.
                    // Упростим: копаем 3x3 по горизонтали и 2 по вертикали (включая целевой уровень и выше/ниже)
                    // Для вертикали нужно уточнить, но пока оставим как есть – область 3x3x1 перед головкой.
                    // Лучше пропустить, так как вертикальное бурение редкость.
                    targets.add(front.offset(xOffset, 0, zOffset));
                    targets.add(front.offset(xOffset, 1, zOffset)); // и выше
                }
            }
        }
        // Убираем дубликаты (на случай если сетка пересекается)
        return targets.stream().distinct().collect(Collectors.toList());
    }

    private boolean tryBreakBlock(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(DrillHeadBlock.FACING);
        List<BlockPos> breakPositions = getBlocksToBreak(pos, facing);

        // Проверяем, может ли разместитель построить следующий блок (хотя бы один сломаем)
        if (placerPos != null && level.getBlockEntity(placerPos) instanceof ShaftPlacerBlockEntity placer) {
            if (!placer.hasResourcesForNext()) {
                return false; // не хватает ресурсов – не бурим
            }
        }

        boolean anyBroken = false;
        List<ItemStack> allDrops = new ArrayList<>();

        for (BlockPos breakPos : breakPositions) {
            BlockState targetState = level.getBlockState(breakPos);
            if (targetState.isAir()) continue;
            float destroySpeed = targetState.getDestroySpeed(level, breakPos);
            if (destroySpeed < 0 || destroySpeed > 50) continue; // неломаемые или слишком крепкие

            // Собираем дроп
            List<ItemStack> drops = Block.getDrops(targetState, (ServerLevel) level, breakPos, level.getBlockEntity(breakPos));
            allDrops.addAll(drops);
            level.destroyBlock(breakPos, false);
            anyBroken = true;
        }

        if (!anyBroken) return false;

        // Логика сбора лута в MiningPort через Placer
        boolean collected = false;
        if (placerPos != null && level.getBlockEntity(placerPos) instanceof ShaftPlacerBlockEntity placer) {
            BlockPos portPos = placer.getMiningPortPos();
            if (portPos != null && level.getBlockEntity(portPos) instanceof MiningPortBlockEntity port) {
                for (ItemStack stack : allDrops) {
                    ItemStack remainder = port.addItem(stack);
                    if (!remainder.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
                    }
                }
                collected = true;
            }
        }

        if (!collected) {
            for (ItemStack stack : allDrops) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }

        return true;
    }

    private void handleClientAnimation() {
        float targetSpeed = (speed > 0) ? Math.max(0.1f, speed / 100f) : 0f;
        if (targetSpeed > 0) {
            ticksWithoutPower = 0;
            if (currentAnimationSpeed < targetSpeed) currentAnimationSpeed = Math.min(currentAnimationSpeed + ACCELERATION, targetSpeed);
            else if (currentAnimationSpeed > targetSpeed) currentAnimationSpeed = Math.max(currentAnimationSpeed - ACCELERATION, targetSpeed);
        } else {
            if (currentAnimationSpeed > 0) {
                ticksWithoutPower++;
                if (ticksWithoutPower > STOP_DELAY_TICKS) currentAnimationSpeed = Math.max(currentAnimationSpeed - DECELERATION, 0f);
            } else ticksWithoutPower = 0;
        }
    }

    @Override
    public void setSpeed(long speed) {
        this.speed = speed;
        setChanged();
        sync();
        invalidateNeighborCaches(); // добавить
    }

    @Override
    public void setTorque(long torque) {
        this.torque = torque;
        setChanged();
        sync();
        invalidateNeighborCaches(); // добавить
    }

    private void moveForward(Level level, BlockPos oldPos, BlockState oldState) {
        Direction facing = oldState.getValue(DrillHeadBlock.FACING);
        BlockPos newPos = oldPos.relative(facing);

        // Сохраняем данные
        long currentSpeed = this.speed;
        long currentTorque = this.torque;
        BlockPos currentPlacerPos = this.placerPos;

        // Удаляем старую головку
        level.removeBlock(oldPos, false);

        // Устанавливаем новую головку
        BlockState newState = oldState.setValue(DrillHeadBlock.FACING, facing);
        level.setBlock(newPos, newState, 3);
        BlockEntity newBe = level.getBlockEntity(newPos);
        if (newBe instanceof DrillHeadBlockEntity newDrill) {
            newDrill.setSpeed(currentSpeed);
            newDrill.setTorque(currentTorque);
            newDrill.setPlacerPos(currentPlacerPos);
            newDrill.lastBreakTick = this.lastBreakTick;

            // НЕМЕДЛЕННО обновляем источник для новой головки
            long currentTime = level.getGameTime();
            RotationSource source = RotationNetworkHelper.findSource(newDrill, null);
            newDrill.setCachedSource(source, currentTime);
            newDrill.setSpeed(source != null ? source.speed() : 0);
            newDrill.setTorque(source != null ? source.torque() : 0);
        }

        // Сообщаем разместителю о перемещении
        if (currentPlacerPos != null && level.getBlockEntity(currentPlacerPos) instanceof ShaftPlacerBlockEntity placer) {
            placer.handleHeadMoved(oldPos, newPos);
        }
    }

    // GeckoLib
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "drill_controller", 0, this::animationPredicate));
    }
    private <E extends GeoBlockEntity> PlayState animationPredicate(AnimationState<E> event) {
        if (currentAnimationSpeed < MIN_ANIM_SPEED) return PlayState.STOP;
        event.getController().setAnimation(ROTATION);
        event.getController().setAnimationSpeed(currentAnimationSpeed);
        return PlayState.CONTINUE;
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    // NBT
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Speed", speed);
        tag.putLong("Torque", torque);
        tag.putLong("LastBreakTick", lastBreakTick); // добавить
        if (placerPos != null) {
            tag.putLong("PlacerPos", placerPos.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        speed = tag.getLong("Speed");
        torque = tag.getLong("Torque");
        lastBreakTick = tag.getLong("LastBreakTick"); // загружаем
        cachedSource = null;
        placerPos = tag.contains("PlacerPos") ? BlockPos.of(tag.getLong("PlacerPos")) : null;
    }
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}