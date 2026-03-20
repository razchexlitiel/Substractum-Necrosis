package com.cim.multiblock.industrial;

import com.cim.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HeaterBlockEntity extends BlockEntity {
    private int heatLevel = 0;
    private static final int MAX_HEAT = 1000;

    public HeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEATER_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HeaterBlockEntity be) {
        if (be.heatLevel < MAX_HEAT) {
            be.heatLevel++;
            if (level.getGameTime() % 20 == 0) {
                be.setChanged();
            }
        }
    }

    public InteractionResult onUse(Player player) {
        if (!level.isClientSide) {
            player.sendSystemMessage(Component.literal("§6[HEATER] §fHeat: §c" + heatLevel + "§f/§c" + MAX_HEAT));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Heat", heatLevel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heatLevel = tag.getInt("Heat");
    }
}