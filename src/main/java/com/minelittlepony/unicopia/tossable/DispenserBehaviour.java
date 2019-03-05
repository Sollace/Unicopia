package com.minelittlepony.unicopia.tossable;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

class DispenserBehaviour extends BehaviorDefaultDispenseItem {
    @Override
    public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
        ITossableItem tossable = (ITossableItem)stack.getItem();

        if (tossable.canBeThrown(stack)) {
            return shootStack(source, stack);
        }

        return super.dispenseStack(source, stack);
    }

    public ItemStack shootStack(IBlockSource source, ItemStack stack) {
        return ((ITossableItem)stack.getItem()).toss(source.getWorld(),
                BlockDispenser.getDispensePosition(source),
                (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING),
                stack, getProjectileInaccuracy(), getProjectileVelocity());
    }

    protected float getProjectileInaccuracy() {
        return 6.0F;
    }

    protected float getProjectileVelocity() {
        return 1.1F;
    }
}