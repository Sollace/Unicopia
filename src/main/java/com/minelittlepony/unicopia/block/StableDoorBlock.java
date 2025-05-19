package com.minelittlepony.unicopia.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class StableDoorBlock extends DoorBlock {
    public static final MapCodec<StableDoorBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockSetType.CODEC.fieldOf("block_set_type").forGetter(StableDoorBlock::getBlockSetType),
            DoorBlock.createSettingsCodec()
    ).apply(instance, StableDoorBlock::new));

    public StableDoorBlock(BlockSetType blockSet, Settings settings) {
        super(blockSet, settings);
    }

    @Override
    public MapCodec<? extends StableDoorBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.get(HALF);

        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (neighborState.isOf(this) && neighborState.get(HALF) != half) {
                state = state
                        .with(FACING, neighborState.get(FACING))
                        .with(HINGE, neighborState.get(HINGE));
                if (half ==  DoubleBlockHalf.UPPER && direction == Direction.DOWN && !state.get(POWERED)) {
                    state = state.with(OPEN, neighborState.get(OPEN));
                }
                return state;
            }

            return Blocks.AIR.getDefaultState();
        }

        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return state;
    }
}
