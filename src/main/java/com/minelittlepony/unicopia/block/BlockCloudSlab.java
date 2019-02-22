package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockCloudSlab<T extends Block & ICloudBlock> extends BlockSlab implements ICloudBlock {

    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.<Variant>create("variant", Variant.class);

    protected final T modelBlock;

    public BlockCloudSlab(T modelBlock, BlockCloudSlab<? extends T> single, Material material, String domain, String name) {
        super(material);

        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(0.5F);
        setResistance(1);
        setSoundType(SoundType.CLOTH);
        setLightOpacity(20);
        setTranslationKey(name);
        setRegistryName(domain, name);

        useNeighborBrightness = true;

        this.modelBlock = modelBlock;
        this.fullBlock = isDouble();
    }

    @Deprecated
    @Override
    public boolean isTranslucent(IBlockState state) {
        return modelBlock.isTranslucent(state);
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return modelBlock.isAir(state, world, pos);
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return isDouble() && modelBlock != null && modelBlock.isOpaqueCube(state);
    }

    @Deprecated
    @Override
    public boolean isFullCube(IBlockState state) {
        return isDouble() && modelBlock.isFullCube(state);
    }

    @Deprecated
    @Override
    public boolean isNormalCube(IBlockState state) {
        return isDouble() && modelBlock.isNormalCube(state);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return modelBlock.getRenderLayer();
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return super.isPassable(worldIn, pos);
    }

    @Override
    public void onFallenUpon(World w, BlockPos pos, Entity entity, float fallDistance) {
        modelBlock.onFallenUpon(w, pos, entity, fallDistance);
    }

    @Override
    public void onLanded(World w, Entity entity) {
        modelBlock.onLanded(w, entity);
    }

    @Override
    public void onEntityCollision(World w, BlockPos pos, IBlockState state, Entity entity) {
        modelBlock.onEntityCollision(w, pos, state, entity);
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

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        return modelBlock.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return modelBlock.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public String getTranslationKey(int meta) {
        return super.getTranslationKey();
    }

    @Override
    public IProperty<Variant> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return modelBlock.getCloudMaterialType(blockState);
    }

    @Override
    public Comparable<Variant> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
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

        @Override
        public IBlockState getStateFromMeta(int meta) {
            return getDefaultState()
                    .withProperty(VARIANT, Variant.DEFAULT)
                    .withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }

        @Override
        public int getMetaFromState(IBlockState state) {
            int i = 0;

            if (state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
                i |= 8;
            }

            return i;
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, HALF, VARIANT);
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

        @Override
        public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
            return new ItemStack(getItemDropped(state, world.rand, 0));
        }

        @Override
        public IBlockState getStateFromMeta(int meta) {
            return getDefaultState().withProperty(VARIANT, Variant.DEFAULT);
        }

        @Override
        public int getMetaFromState(IBlockState state) {
            return 0;
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, VARIANT);
        }
    }

    private static enum Variant implements IStringSerializable {
        DEFAULT;

        public String getName() {
            return "normal";
        }
    }
}
