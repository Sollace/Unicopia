package com.minelittlepony.unicopia.item.override;

import com.minelittlepony.unicopia.UItems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemShear extends ShearsItem {
    @Override
    public EnumActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        BlockState state = world.getBlockState(pos);

        if (UItems.moss.tryConvert(world, state, pos, player)) {
            ItemStack stack = player.getStackInHand(hand);

            if (!player.isCreative()) {
                stack.damageItem(1, player);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }
}
