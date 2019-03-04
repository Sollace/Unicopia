package com.minelittlepony.unicopia.block;

import java.util.Random;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class BlockSugar extends BlockFalling {

    public BlockSugar(String domain, String name) {
        super(Material.SAND);
        setTranslationKey(name);
        setRegistryName(domain, name);

        setSoundType(SoundType.SAND);
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setHardness(0.7F);
    }

    @Override
    public int quantityDropped(Random random) {
        return 9;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.SUGAR;
    }
}
