package com.minelittlepony.unicopia.block;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public interface SegmentedBlock {
    double[] STAGE_SIZES = new double[] {3.2, 6.4, 9.6, 12.8, 16};
    VoxelShape[] STAGE_SHAPES = Arrays.stream(STAGE_SIZES).mapToObj(height -> {
        return Block.createCuboidShape(0, 0, 0, 16, height, 16);
    }).toArray(VoxelShape[]::new);

    static VoxelShape getShape(int age) {
        if (age < 0 || age >= STAGE_SHAPES.length) {
            return VoxelShapes.fullCube();
        }
        return STAGE_SHAPES[age];
    }

    static double getHeight(int age) {
        if (age < 0 || age >= STAGE_SHAPES.length) {
            return 16;
        }
        return STAGE_SIZES[age];
    }

    static VoxelShape[] computeShapes(int maxHeight) {
       VoxelShape[] shapes = new VoxelShape[STAGE_SIZES.length * maxHeight];

       for (int i = 0; i < maxHeight; i++) {
           for (int j = 0; j < STAGE_SIZES.length; j++) {
               shapes[j + (i * STAGE_SIZES.length)] = Block.createCuboidShape(0, 0, 0, 16, STAGE_SIZES[j] + (i * 16), 16);
           }
       }

       return shapes;
    }

    boolean isBase(BlockState state);

    boolean isNext(BlockState state);

    default BlockPos getTip(BlockView world, BlockPos startingPos) {
        while (isNext(world.getBlockState(startingPos.up())) && !world.isOutOfHeightLimit(startingPos)) {
            startingPos = startingPos.up();
        }

        return startingPos;
    }

    default BlockPos getRoot(BlockView world, BlockPos startingPos) {
        while (isBase(world.getBlockState(startingPos.down())) && !world.isOutOfHeightLimit(startingPos)) {
            startingPos = startingPos.down();
        }

        return startingPos;
    }
}
