package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.forgebullshit.FUF;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockCloudSlab<T extends Block & ICloudBlock> extends USlab<T> implements ICloudBlock {

    public BlockCloudSlab(T modelBlock, BlockCloudSlab<? extends T> single, Material material, String domain, String name) {
        super(modelBlock, single, material, domain, name);
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
    @Deprecated
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
        if (getCanInteract(state, entity)) {
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

    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return modelBlock.getCloudMaterialType(blockState);
    }

    public static class Single<T extends Block & ICloudBlock> extends BlockCloudSlab<T> {

        public final Double<T> doubleSlab;

        public Single(T modelBlock, Material material, String domain, String name) {
            super(modelBlock, null, material, domain, name);

            doubleSlab = new Double<>(this, domain, "double_" + name);
        }

        @Override
        public boolean isDouble() {
            return false;
        }

        @Override
        public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {

            IBlockState beside = world.getBlockState(pos.offset(face));

            if (beside.getBlock() instanceof ICloudBlock) {
                ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

                if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {

                    EnumBlockHalf half = state.getValue(HALF);

                    if (beside.getBlock() instanceof BlockCloudStairs) {
                        return beside.getValue(BlockStairs.HALF).ordinal() == state.getValue(HALF).ordinal()
                           && beside.getValue(BlockStairs.FACING) == face;
                    }

                    if (face == EnumFacing.DOWN) {
                        return half == EnumBlockHalf.BOTTOM;
                    }

                    if (face == EnumFacing.UP) {
                        return half == EnumBlockHalf.TOP;
                    }

                    if (beside.getBlock() == this) {
                        return beside.getValue(HALF) == state.getValue(HALF);
                    }
                }
            }

            return false;
        }
    }

    public static class Double<T extends Block & ICloudBlock> extends BlockCloudSlab<T> {

        public final Single<T> singleSlab;

        public Double(Single<T> single, String domain, String name) {
            super(single.modelBlock, single, single.material, domain, name);

            this.singleSlab = single;
        }

        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {

            IBlockState beside = world.getBlockState(pos.offset(face));

            if (beside.getBlock() instanceof ICloudBlock) {
                ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

                if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public Item getItemDropped(IBlockState state, Random rand, int fortune) {
            return Item.getItemFromBlock(singleSlab);
        }
    }
}
