package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockCloudStairs extends UStairs implements ICloudBlock {

    public BlockCloudStairs(BlockState inherited, String domain, String name) {
        super(inherited, domain, name);
    }

    @Override
    public boolean isAir(BlockState state, BlockView world, BlockPos pos) {
        return allowsFallingBlockToPass(state, world, pos);
    }

    @Override
    public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, Box entityBox, List<Box> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
        if (getCanInteract(baseBlockState, entity)) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, p_185477_7_);
        }
    }

    @Deprecated
    @Override
    public HitResult collisionRayTrace(BlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        if (handleRayTraceSpecialCases(worldIn, pos, blockState)) {
            return null;
        }
        return super.collisionRayTrace(blockState, worldIn, pos, start, end);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isTopSolid(BlockState state) {
        return getCloudMaterialType(state) == CloudType.ENCHANTED && super.isTopSolid(state);
    }

    @SuppressWarnings("deprecation")
    @FUF(reason = "...Really?")
    public boolean isSideSolid(BlockState base_state, BlockView world, BlockPos pos, Direction side) {
        return getCloudMaterialType(base_state) == CloudType.ENCHANTED && super.isSideSolid(base_state, world, pos, side);
    }

    @Override
    public boolean doesSideBlockRendering(BlockState state, BlockView world, BlockPos pos, Direction face) {

        BlockState beside = world.getBlockState(pos.offset(face));

        if (beside.getBlock() instanceof ICloudBlock) {
            ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

            if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                Direction front = state.get(FACING);
                BlockHalf half = state.get(HALF);

                VoxelShape shape = state.getCollisionShape(world, pos);

                boolean sideIsBack = Block.isFaceFullSquare(shape, face);
                boolean sideIsFront = Block.isFaceFullSquare(shape, face.getOpposite());
                boolean sideIsSide = !(sideIsBack || sideIsFront);

                if (beside.getBlock() == this) {
                    Direction bfront = beside.get(FACING);
                    BlockHalf bhalf = beside.get(HALF);

                    if (face == Direction.UP || face == Direction.DOWN) {
                        return half != bhalf
                                && ( (face == Direction.UP && half == BlockHalf.TOP)
                                  || (face == Direction.DOWN && half == BlockHalf.BOTTOM)
                            );
                    }

                    VoxelShape shapeBeside = beside.getCollisionShape(world, pos);

                    boolean bsideIsBack = Block.isFaceFullSquare(shapeBeside, face);
                    boolean bsideIsFront = Block.isFaceFullSquare(shapeBeside, face.getOpposite());
                    boolean bsideIsSide = !(bsideIsBack || bsideIsFront);

                    return sideIsBack
                            || (sideIsSide && bsideIsSide && front == bfront && half == bhalf);
                } else if (beside.getBlock() instanceof BlockCloudSlab) {
                    SlabType bhalf = beside.get(SlabBlock.TYPE);

                    if (face == Direction.UP || face == Direction.DOWN) {
                        return bhalf == SlabType.TOP && half == BlockHalf.BOTTOM;
                    }

                    return bhalf == SlabType.TOP && half == BlockHalf.BOTTOM;
                } else {
                    if (face == Direction.UP || face == Direction.DOWN) {
                        return half == BlockHalf.BOTTOM && face == Direction.DOWN;
                    }
                }

                return front == face;
            }
        }

        return false;
    }

    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return CloudType.NORMAL;
    }
}