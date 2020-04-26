package com.minelittlepony.unicopia.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class AbstractStairsBlock<T extends Block> extends StairsBlock {

    protected final T baseBlock;
    protected final BlockState baseBlockState;

    @SuppressWarnings("unchecked")
    public AbstractStairsBlock(BlockState inherited, Settings settings) {
        super(inherited, settings);
        baseBlock = (T)inherited.getBlock();
        baseBlockState = inherited;
    }

    @Override
    public boolean canSuffocate(BlockState state, BlockView world, BlockPos pos) {
        return baseBlock.canSuffocate(baseBlockState, world, pos);
    }

    @Override
    public void onLandedUpon(World w, BlockPos pos, Entity entity, float fallDistance) {
        baseBlock.onLandedUpon(w, pos, entity, fallDistance);
    }

    @Override
    public void onEntityLand(BlockView w, Entity entity) {
        baseBlock.onEntityLand(w, entity);
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        baseBlockState.onEntityCollision(w, pos, entity);
    }

    @Override
    public void onSteppedOn(World w, BlockPos pos, Entity entity) {
        baseBlock.onSteppedOn(w, pos, entity);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return baseBlock.calcBlockBreakingDelta(state, player, world, pos);
    }
}
