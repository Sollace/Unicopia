package com.minelittlepony.unicopia.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface GlowableItem {
    default boolean isGlowing(ItemStack stack) {
        NbtCompound tag = stack.getSubTag("display");
        return tag != null && tag.getBoolean("glowing");
    }

    default void setGlowing(ItemStack stack, boolean glowing) {
        stack.getOrCreateSubTag("display").putBoolean("glowing", glowing);
    }
}
