package com.minelittlepony.unicopia.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
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
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class USlab<T extends Block> extends BlockSlab {

    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.<Variant>create("variant", Variant.class);

    protected final T modelBlock;

    public USlab(T modelBlock, USlab<? extends T> single, Material material, String domain, String name) {
        super(material);

        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(0.5F);
        setResistance(1);
        setSoundType(SoundType.CLOTH);
        setLightOpacity(20);
        setTranslationKey(name);
        setTickRandomly(modelBlock.getTickRandomly());
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
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        modelBlock.updateTick(world, pos, state, rand);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return modelBlock.getRenderLayer();
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
    public Comparable<Variant> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(getItemDropped(state, world.rand, 0));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (isDouble()) {
            return getDefaultState().withProperty(VARIANT, Variant.DEFAULT);
        }

        return getDefaultState()
                .withProperty(VARIANT, Variant.DEFAULT)
                .withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (isDouble()) {
            return 0;
        }

        int i = 0;

        if (state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            i |= 8;
        }

        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        if (isDouble()) {
            return new BlockStateContainer(this, VARIANT);
        }

        return new BlockStateContainer(this, HALF, VARIANT);
    }

    public static class Single<T extends Block> extends USlab<T> {

        public final Double<T> doubleSlab;

        public Single(T modelBlock, Material material, String domain, String name) {
            super(modelBlock, null, material, domain, name);

            doubleSlab = new Double<>(this, domain, "double_" + name);
        }

        @Override
        public boolean isDouble() {
            return false;
        }
    }

    public static class Double<T extends Block> extends USlab<T> {

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
        public Item getItemDropped(IBlockState state, Random rand, int fortune) {
            return Item.getItemFromBlock(singleSlab);
        }
    }

    private static enum Variant implements IStringSerializable {
        DEFAULT;

        public String getName() {
            return "normal";
        }
    }
}
