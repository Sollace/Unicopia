package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.block.BlockTomatoPlant;

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

        if (state.getBlock() instanceof BlockTomatoPlant) {
            if (((BlockTomatoPlant)state.getBlock()).plant(world, pos, state)) {
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }
}
