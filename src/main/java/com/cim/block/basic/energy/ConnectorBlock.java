package com.cim.block.basic.energy;

import com.cim.block.entity.energy.ConnectorBlockEntity;
import com.cim.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class ConnectorBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // Коллизии 4x4x6 пикселей для каждого facing.
    // facing = направление, КУДА смотрит верхушка (куда растёт от грани)
    // Коннектор на верхней грани блока земли → facing=UP → растёт вверх
    private static final VoxelShape SHAPE_UP    = Block.box(6, 0, 6, 10, 6, 10);    // на полу, растёт вверх
    private static final VoxelShape SHAPE_DOWN  = Block.box(6, 10, 6, 10, 16, 10);  // на потолке, растёт вниз
    private static final VoxelShape SHAPE_NORTH = Block.box(6, 6, 10, 10, 10, 16);  // на южной стене, растёт на север
    private static final VoxelShape SHAPE_SOUTH = Block.box(6, 6, 0, 10, 10, 6);    // на северной стене, растёт на юг
    private static final VoxelShape SHAPE_WEST  = Block.box(10, 6, 6, 16, 10, 10);  // на восточной стене, растёт на запад
    private static final VoxelShape SHAPE_EAST  = Block.box(0, 6, 6, 6, 10, 10);    // на западной стене, растёт на восток

    public ConnectorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // facing = направление ОТ грани наружу (куда смотрит верхушка)
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP    -> SHAPE_UP;
            case DOWN  -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED; // GeckoLib!
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConnectorBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ConnectorBlockEntity connector) {
                connector.onRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}