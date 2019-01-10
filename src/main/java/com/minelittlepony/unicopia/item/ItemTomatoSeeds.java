package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.block.BlockTomatoPlant;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTomatoSeeds extends Item {

    public ItemTomatoSeeds(String domain, String name) {
        setTranslationKey(name);
        setRegistryName(domain, name);
        setCreativeTab(CreativeTabs.MATERIALS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        IBlockState state = world.getBlockState(pos);

        Block block = state.getBlock();

        if (block instanceof BlockTomatoPlant) {
            if (((BlockTomatoPlant)block).plant(world, pos, state)) {
                player.getHeldItem(hand).shrink(1);

                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }
}
