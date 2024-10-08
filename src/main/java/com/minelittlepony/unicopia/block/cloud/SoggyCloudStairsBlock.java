package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.state.StateUtil;
import com.minelittlepony.unicopia.util.CodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SoggyCloudStairsBlock extends CloudStairsBlock implements Soakable {
    private static final MapCodec<SoggyCloudStairsBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("base_state").forGetter(block -> block.baseBlockState),
            CodecUtils.supplierOf(Registries.BLOCK.getCodec()).optionalFieldOf("soggy_block", null).forGetter(b -> b.dryBlock),
            StairsBlock.createSettingsCodec()
    ).apply(instance, SoggyCloudStairsBlock::new));

    private final Supplier<Block> dryBlock;

    public SoggyCloudStairsBlock(BlockState baseState, Supplier<Block> dryBlock, Settings settings) {
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
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return dryBlock.get().getPickStack(world, pos, state);
    }

    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return Soakable.tryCollectMoisture(state, world, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockState getStateWithMoisture(BlockState state, int moisture) {
        if (moisture <= 0) {
            return StateUtil.copyState(state, dryBlock.get().getDefaultState());
        }
        return StateUtil.copyState(state, getDefaultState()).with(MOISTURE, moisture);
    }
}
