package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class BlockCloudSlab<T extends Block & ICloudBlock> extends USlab<T> implements ICloudBlock {

    public BlockCloudSlab(T modelBlock, BlockCloudSlab<? extends T> single, Material material, String domain, String name) {
        super(modelBlock, single, material, domain, name);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isTopSolid(BlockState state) {
        return getCloudMaterialType(state) == CloudType.ENCHANTED && super.isTopSolid(state);
    }

    @SuppressWarnings("deprecation")
    @FUF(reason = "...Really?")
    public boolean isSideSolid(BlockState base_state, BlockAccess world, BlockPos pos, Direction side) {
        return getCloudMaterialType(base_state) == CloudType.ENCHANTED && super.isSideSolid(base_state, world, pos, side);
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, Box entityBox, List<Box> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
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
    public CloudType getCloudMaterialType(BlockState blockState) {
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
        public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {

            if (beside.getBlock() instanceof ICloudBlock) {
                ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

                if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {

                    BlockHalf half = state.get(HALF);

                    if (beside.getBlock() instanceof BlockCloudStairs) {
                        return beside.get(StairsBlock.HALF).ordinal() == state.get(HALF).ordinal()
                           && beside.get(Properties.FACING) == face;
                    }

                    if (face == Direction.DOWN) {
                        return half == BlockHalf.BOTTOM;
                    }

                    if (face == Direction.UP) {
                        return half == BlockHalf.TOP;
                    }

                    if (beside.getBlock() == this) {
                        return beside.get(HALF) == state.get(HALF);
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
