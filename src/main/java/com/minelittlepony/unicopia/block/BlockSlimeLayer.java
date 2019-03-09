package com.minelittlepony.unicopia.block;

import java.util.Random;

import net.minecraft.block.BlockSnow;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSlimeLayer extends BlockSnow {

    public BlockSlimeLayer(String domain, String name) {
        setTranslationKey(name);
        setRegistryName(domain, name);

        setSoundType(SoundType.SLIME);
        setCreativeTab(CreativeTabs.DECORATIONS);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Deprecated
    public Material getMaterial(IBlockState state) {
        return Material.CLAY;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return MapColor.GRASS;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.SLIME_BALL;
    }

    @Deprecated
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {

        float factor = getMotionFactor(world.getBlockState(pos));

        entity.motionX *= factor;
        entity.motionY *= factor;
        entity.motionZ *= factor;
    }

    protected float getMotionFactor(IBlockState state) {
        return 1/state.getValue(LAYERS);
    }
}
