package com.minelittlepony.unicopia.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SmartStairsBlock extends StairsBlock {

    protected final BlockState baseBlockState;

    public SmartStairsBlock(BlockState inherited, Settings settings) {
        super(inherited, settings);
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
}
