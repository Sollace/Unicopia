package com.minelittlepony.unicopia.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class AbstractSlabBlock extends SlabBlock {

    protected final BlockState modelState;

    public AbstractSlabBlock(BlockState inherited, Block.Settings settings) {
        super(settings);
        modelState = inherited;
    }

    @Deprecated
    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return modelState.isTranslucent(world, pos);
    }

    @Override
    public boolean allowsSpawning(BlockState state, BlockView view, BlockPos pos, EntityType<?> type) {
        return modelState.allowsSpawning(view, pos, type);
    }

    @Override
    public boolean isAir(BlockState state) {
        return modelState.isAir();
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
}
