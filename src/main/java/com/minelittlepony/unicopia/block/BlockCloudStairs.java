package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.forgebullshit.FUF;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCloudStairs extends UStairs implements ICloudBlock {

    public BlockCloudStairs(IBlockState inherited, String domain, String name) {
        super(inherited, domain, name);
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return allowsFallingBlockToPass(state, world, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, Box entityBox, List<Box> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
        if (getCanInteract(theState, entity)) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, p_185477_7_);
        }
    }

    @Deprecated
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        if (handleRayTraceSpecialCases(worldIn, pos, blockState)) {
            return null;
        }
        return super.collisionRayTrace(blockState, worldIn, pos, start, end);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isTopSolid(IBlockState state) {
        return getCloudMaterialType(state) == CloudType.ENCHANTED && super.isTopSolid(state);
    }

    @SuppressWarnings("deprecation")
    @FUF(reason = "...Really?")
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return getCloudMaterialType(base_state) == CloudType.ENCHANTED && super.isSideSolid(base_state, world, pos, side);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        state = state.getActualState(world, pos);

        IBlockState beside = world.getBlockState(pos.offset(face)).getActualState(world, pos);

        if (beside.getBlock() instanceof ICloudBlock) {
            ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

            if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                EnumFacing front = state.getValue(FACING);
                EnumHalf half = state.getValue(HALF);

                boolean sideIsBack = state.getBlockFaceShape(world, pos, face) == BlockFaceShape.SOLID;
                boolean sideIsFront = state.getBlockFaceShape(world, pos, face.getOpposite()) == BlockFaceShape.SOLID;
                boolean sideIsSide = !(sideIsBack || sideIsFront);

                if (beside.getBlock() == this) {
                    EnumFacing bfront = beside.getValue(FACING);
                    EnumHalf bhalf = beside.getValue(HALF);

                    if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                        return half != bhalf
                                && ( (face == EnumFacing.UP && half == EnumHalf.TOP)
                                  || (face == EnumFacing.DOWN && half == EnumHalf.BOTTOM)
                            );
                    }

                    boolean bsideIsBack = beside.getBlockFaceShape(world, pos, face) == BlockFaceShape.SOLID;
                    boolean bsideIsFront = beside.getBlockFaceShape(world, pos, face.getOpposite()) == BlockFaceShape.SOLID;
                    boolean bsideIsSide = !(bsideIsBack || bsideIsFront);

                    return sideIsBack
                            || (sideIsSide && bsideIsSide && front == bfront && half == bhalf);
                } else if (beside.getBlock() instanceof BlockCloudSlab) {
                    EnumBlockHalf bhalf = beside.getValue(BlockSlab.HALF);

                    if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                        return bhalf == EnumBlockHalf.TOP && half == EnumHalf.BOTTOM;
                    }

                    return bhalf == EnumBlockHalf.TOP && half == EnumHalf.BOTTOM;
                } else {
                    if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                        return half == EnumHalf.BOTTOM && face == EnumFacing.DOWN;
                    }
                }

                return front == face;
            }
        }

        return false;
    }

    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return CloudType.NORMAL;
    }
}