package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.UBlocks;
import com.minelittlepony.unicopia.block.StickBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
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
    public EnumActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {

        BlockState state = world.getBlockState(pos);

        Block block = state.getBlock();

        if (block instanceof StickBlock) {
            if (UBlocks.tomato_plant.plant(world, pos, state)) {
                if (!player.capabilities.isCreativeMode) {
                    player.getStackInHand(hand).shrink(1);
                }

                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }
}
