package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.state.StateUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class PoreousCloudBlock extends CloudBlock implements Soakable {
    @Nullable
    private final Supplier<Soakable> soggyBlock;

    public PoreousCloudBlock(Settings settings, boolean meltable, @Nullable Supplier<Soakable> soggyBlock) {
        super(settings.nonOpaque(), meltable);
        this.soggyBlock = soggyBlock;
    }

    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return Soakable.tryDepositMoisture(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockState getStateWithMoisture(BlockState state, int moisture) {
        if (moisture <= 0) {
            return StateUtil.copyState(state, getDefaultState());
        }
        return soggyBlock == null ? null : soggyBlock.get().getStateWithMoisture(state, moisture);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.getBlock() instanceof Soakable soakable && world.hasRain(pos) && world.isAir(pos.up())) {
            @Nullable
            BlockState soggyState = soakable.getStateWithMoisture(state, random.nextBetween(1, 5));
            if (soggyState != null) {
                world.setBlockState(pos, soggyState);
                return;
            }
        }

        super.randomTick(state, world, pos, random);
    }
}
