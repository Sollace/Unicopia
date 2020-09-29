package com.minelittlepony.unicopia.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public interface ItemWielder {
    void updateItemUsage(Hand hand, ItemStack stack, int time);
}
