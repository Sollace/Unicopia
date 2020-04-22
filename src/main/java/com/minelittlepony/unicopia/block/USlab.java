package com.minelittlepony.unicopia.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class USlab<T extends Block> extends SlabBlock {

    protected final T modelBlock;

    public USlab(T model, Block.Settings settings) {
        super(settings);
        this.modelBlock = model;
    }

    @Deprecated
    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return modelBlock.isTranslucent(state, world, pos);
    }

    @Override
    public boolean isAir(BlockState state) {
        return modelBlock.isAir(state);
    }

    public boolean isDouble(BlockState state) {
        return state.get(TYPE) == SlabType.DOUBLE;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        modelBlock.scheduledTick(state, world, pos, rand);
    }

    @Override
    public void onLandedUpon(World w, BlockPos pos, Entity entity, float fallDistance) {
        modelBlock.onLandedUpon(w, pos, entity, fallDistance);
    }

    @Override
    public void onEntityLand(BlockView w, Entity entity) {
        modelBlock.onEntityLand(w, entity);
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        modelBlock.onEntityCollision(state, w, pos, entity);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView worldIn, BlockPos pos) {
        return modelBlock.calcBlockBreakingDelta(state, player, worldIn, pos);
    }
}
