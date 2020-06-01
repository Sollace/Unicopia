package com.minelittlepony.unicopia.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class SmartStairsBlock extends StairsBlock {

    protected final BlockState baseBlockState;

    public SmartStairsBlock(BlockState inherited, Settings settings) {
        super(inherited, settings);
        setDefaultState(getDefaultState().with(Covering.PROPERTY, Covering.UNCOVERED));
        baseBlockState = inherited;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return baseBlockState.isTranslucent(world, pos);
    }

    @Override
    public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
        return baseBlockState.allowsSpawning(view, pos, type);
    }

    @Override
    public void onLandedUpon(World w, BlockPos pos, Entity entity, float fallDistance) {
        baseBlockState.getBlock().onLandedUpon(w, pos, entity, fallDistance);
    }

    @Override
    public void onEntityLand(BlockView w, Entity entity) {
        baseBlockState.getBlock().onEntityLand(w, entity);
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        baseBlockState.onEntityCollision(w, pos, entity);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return baseBlockState.calcBlockBreakingDelta(player, world, pos);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        return super.getPlacementState(context).with(Covering.PROPERTY, Covering.getCovering(context.getWorld(), context.getBlockPos().up()));
    }

    @Override
    @Deprecated
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState other, IWorld world, BlockPos pos, BlockPos otherPos) {
        state = super.getStateForNeighborUpdate(state, direction, other, world, pos, otherPos);

        if (direction == Direction.UP) {
            return state.with(Covering.PROPERTY, Covering.getCovering(world, otherPos));
        }

        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Covering.PROPERTY);
    }
}
