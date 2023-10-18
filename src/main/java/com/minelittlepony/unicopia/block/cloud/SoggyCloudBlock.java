package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class SoggyCloudBlock extends CloudBlock implements Soakable {

    private final Supplier<Block> dryBlock;

    public SoggyCloudBlock(Settings settings, Supplier<Block> dryBlock) {
        super(settings.ticksRandomly(), false);
        setDefaultState(getDefaultState().with(MOISTURE, 7));
        this.dryBlock = dryBlock;
    }

    @Override
    public BlockState getSoggyState(int moisture) {
        return getDefaultState().with(MOISTURE, moisture);
    }

    @Override
    public int getMoisture(BlockState state) {
        return state.get(MOISTURE);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MOISTURE);
    }

    @Override
    @Deprecated
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return Soakable.tryCollectMoisture(dryBlock.get(), state, world, pos, player, hand, hit);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        Soakable.addMoistureParticles(state, world, pos, random);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        Soakable.tickMoisture(dryBlock.get(), state, world, pos, random);
    }
}
