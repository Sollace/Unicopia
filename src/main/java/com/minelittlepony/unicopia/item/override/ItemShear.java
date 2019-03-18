package com.minelittlepony.unicopia.item.override;

import com.minelittlepony.unicopia.init.UItems;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemShear extends ItemShears {
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);

        if (UItems.moss.tryConvert(world, state, pos, player)) {
            ItemStack stack = player.getHeldItem(hand);

            if (!player.isCreative()) {
                stack.damageItem(1, player);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }
}
