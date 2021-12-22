package com.minelittlepony.unicopia.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public interface GlowableItem {
    default boolean isGlowing(ItemStack stack) {
        NbtCompound tag = stack.getSubNbt("display");
        return tag != null && tag.getBoolean("glowing");
    }

    default void setGlowing(ItemStack stack, boolean glowing) {
        stack.getOrCreateSubNbt("display").putBoolean("glowing", glowing);
    }
}
