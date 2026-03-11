package com.cim.block.basic.energy;

import com.cim.block.entity.energy.ConnectorBlockEntity;
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
    private static final VoxelShape SHAPE_UP    = Block.box(6, 0, 6, 10, 6, 10);
    private static final VoxelShape SHAPE_DOWN  = Block.box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_NORTH = Block.box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SHAPE_WEST  = Block.box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_EAST  = Block.box(0, 6, 6, 6, 10, 10);

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
        // Коннектор всегда "растет" от той грани, на которую мы кликнули
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
        // ВАЖНО: Теперь это обычная модель, а не энтити!
        return RenderShape.MODEL;
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