package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.state.StateUtil;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;

public class PoreousCloudStairsBlock extends CloudStairsBlock implements Soakable {
    private static final MapCodec<PoreousCloudStairsBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("base_state").forGetter(block -> block.baseBlockState),
            CodecUtils.supplierOf(Soakable.CODEC).optionalFieldOf("soggy_block", null).forGetter(b -> b.soggyBlock),
            StairsBlock.createSettingsCodec()
    ).apply(instance, PoreousCloudStairsBlock::new));

    protected final Supplier<Soakable> soggyBlock;

    public PoreousCloudStairsBlock(BlockState baseState, Supplier<Soakable> soggyBlock, Settings settings) {
        super(baseState, settings);
        this.soggyBlock = soggyBlock;
    }

    @Override
    public MapCodec<? extends PoreousCloudStairsBlock> getCodec() {
        return CODEC;
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
