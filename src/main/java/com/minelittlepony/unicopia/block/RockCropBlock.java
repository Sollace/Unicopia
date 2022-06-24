package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemConvertible;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class RockCropBlock extends CropBlock {
    private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[] {
            VoxelShapes.union(
                    createCuboidShape(7, -1, 11, 8, 0, 12),
                    createCuboidShape(3.5F, -1, 3.5F, 5, 0, 5),
                    createCuboidShape(11.5F, -1, 5.5F, 13, 0, 7)
            ),
            VoxelShapes.union(
                    createCuboidShape(6, -1, 10, 9, 1, 13),
                    createCuboidShape(2.5F, -1, 2.5F, 6.5F, 2, 6.5F),
                    createCuboidShape(10.5F, -1, 4.5F, 14.5F, 2, 8.5F)
            ),
            VoxelShapes.union(
                    createCuboidShape(4, -1, 9, 10, 2, 14),
                    createCuboidShape(1.5F, -1, 1.5F, 7.5F, 3, 7.5F),
                    createCuboidShape(9, -1, 3.5F, 15.5F, 3, 9.5F)
            ),
            VoxelShapes.union(
                    createCuboidShape(4, -1, 9, 10, 2, 14),
                    createCuboidShape(1.5F, -1, 1.5F, 7.5F, 4, 7.5F),
                    createCuboidShape(9, -1, 3.5F, 15.5F, 3, 9.5F)
            ),
            VoxelShapes.union(
                    createCuboidShape(3, -1, 8, 11, 3, 15),
                    createCuboidShape(0.5F, -1, 0.5F, 8.5F, 5, 8.5F),
                    createCuboidShape(7.5F, -1, 2, 17, 5, 10.5F)
            ),
            VoxelShapes.union(
                    createCuboidShape(3, -1, 8, 11, 4, 15),
                    createCuboidShape(0.5F, -1, 0.5F, 8.5F, 5, 8.5F),
                    createCuboidShape(7.5F, -1, 2, 17, 7, 10.5F)
            ),
            VoxelShapes.union(
                    createCuboidShape(3, -1, 8, 11, 4, 15),
                    createCuboidShape(0.5F, -1, 0.5F, 8.5F, 7, 8.5F),
                    createCuboidShape(7.5F, -1, 2, 17, 9, 10.5F)
            ),
            VoxelShapes.union(
                    createCuboidShape(3, -1, 8, 11, 4, 15),
                    createCuboidShape(0.5F, -1, 0.5F, 8.5F, 9, 8.5F),
                    createCuboidShape(7.5F, -1, 2, 17, 13, 10.5F)
            )
    };

    protected RockCropBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return AGE_TO_SHAPE[state.get(getAgeProperty())];
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (canGrow(world, random, pos, state)) {
            super.randomTick(state, world, pos, random);
        }
    }

    @Override
    @Deprecated
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!moved && !(state.getBlock() == this && newState.getBlock() == this)) {
            if (!world.isClient) {
                world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0);
            }
        }
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 20, EquinePredicates.PLAYER_CAN_USE_EARTH) != null;
    }

    @Override
    public boolean isFertilizable(BlockView world, BlockPos pos, BlockState state, boolean isClient) {
        if (world instanceof World && !canGrow((World)world, ((World)world).random, pos, state)) {
            return false;
        }
        return super.isFertilizable(world, pos, state, isClient);
    }

    @Override
    protected ItemConvertible getSeedsItem() {
        return UItems.PEBBLES;
    }
}
