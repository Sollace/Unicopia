package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.state.StateUtil;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SoggyCloudStairsBlock extends CloudStairsBlock implements Soakable {
    private static final MapCodec<SoggyCloudStairsBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("base_state").forGetter(block -> block.baseBlockState),
            CodecUtils.supplierOf(BlockState.CODEC).optionalFieldOf("soggy_block", null).forGetter(b -> b.dryBlock),
            StairsBlock.createSettingsCodec()
    ).apply(instance, SoggyCloudStairsBlock::new));

    private final Supplier<BlockState> dryBlock;

    public SoggyCloudStairsBlock(BlockState baseState, Supplier<BlockState> dryBlock, Settings settings) {
        super(baseState, settings);
        setDefaultState(getDefaultState().with(MOISTURE, 7));
        this.dryBlock = dryBlock;
    }

    @Override
    public MapCodec<? extends SoggyCloudStairsBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(MOISTURE);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return getStateWithMoisture(state, 0).getPickStack(world, pos, includeData);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return Soakable.tryCollectMoisture(stack, state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockState getStateWithMoisture(BlockState state, int moisture) {
        if (moisture <= 0) {
            return StateUtil.copyState(state, dryBlock.get());
        }
        return StateUtil.copyState(state, getDefaultState()).with(MOISTURE, moisture);
    }
}
