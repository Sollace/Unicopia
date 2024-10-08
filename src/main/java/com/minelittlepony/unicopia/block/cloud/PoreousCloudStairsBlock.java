package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.state.StateUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PoreousCloudStairsBlock extends CloudStairsBlock implements Soakable {

    protected final Supplier<Soakable> soggyBlock;

    public PoreousCloudStairsBlock(BlockState baseState, Settings settings, Supplier<Soakable> soggyBlock) {
        super(baseState, settings);
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
        return soggyBlock.get().getStateWithMoisture(state, moisture);
    }
}
