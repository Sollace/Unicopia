package com.minelittlepony.unicopia.block.zap;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ZapStairsBlock extends StairsBlock {
    public static final MapCodec<ZapStairsBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("base_state").forGetter(block -> block.baseBlock),
            createSettingsCodec()
    ).apply(instance, ZapStairsBlock::new));

    private final BlockState baseBlock;

    public ZapStairsBlock(BlockState baseBlockState, Settings settings) {
        super(baseBlockState, settings);
        this.baseBlock = baseBlockState;
    }

    @Override
    public MapCodec<? extends StairsBlock> getCodec() {
        return CODEC;
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return baseBlock.calcBlockBreakingDelta(player, world, pos);
    }
}
