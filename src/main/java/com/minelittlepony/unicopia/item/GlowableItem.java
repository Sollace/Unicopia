package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.item.component.UDataComponentTypes;

import net.minecraft.item.ItemStack;

public interface GlowableItem {
    static boolean isGlowing(ItemStack stack) {
        Boolean glowing = stack.get(UDataComponentTypes.GLOWING);
        return glowing != null && glowing;
    }

    static void setGlowing(ItemStack stack, boolean glowing) {
        stack.set(UDataComponentTypes.GLOWING, glowing);
    }
}
