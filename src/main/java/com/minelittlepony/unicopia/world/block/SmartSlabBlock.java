package com.minelittlepony.unicopia.world.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class SmartSlabBlock extends SlabBlock {

    protected final BlockState modelState;

    public SmartSlabBlock(BlockState inherited, Block.Settings settings) {
        super(settings);
        modelState = inherited;
    }

    @Deprecated
    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return modelState.isTranslucent(world, pos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        modelState.scheduledTick(world, pos, rand);
    }

    @Override
    public void onLandedUpon(World w, BlockPos pos, Entity entity, float fallDistance) {
        modelState.getBlock().onLandedUpon(w, pos, entity, fallDistance);
    }

    @Override
    public void onEntityLand(BlockView w, Entity entity) {
        modelState.getBlock().onEntityLand(w, entity);
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        modelState.onEntityCollision(w, pos, entity);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView worldIn, BlockPos pos) {
        return modelState.calcBlockBreakingDelta(player, worldIn, pos);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        return super.getPlacementState(context).with(Covering.PROPERTY, Covering.getCovering(context.getWorld(), context.getBlockPos().up()));
    }

    @Override
    @Deprecated
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState other, WorldAccess world, BlockPos pos, BlockPos otherPos) {
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
