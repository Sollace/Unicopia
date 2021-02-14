package com.minelittlepony.unicopia.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface ChameleonItem {

    default boolean isFullyDisguised() {
        return true;
    }

    default ItemStack getAppearanceStack(ItemStack stack) {
        Item appearance = getAppearance(stack);
        if (appearance != Items.AIR) {
            return createAppearanceStack(stack, appearance);
        }
        return stack;
    }

    default ItemStack createAppearanceStack(ItemStack stack, Item appearance) {
        ItemStack newAppearance = new ItemStack(appearance, stack.getCount());
        if (stack.hasTag()) {
            newAppearance.setTag(stack.getTag().copy());
        }
        newAppearance.removeSubTag("appearance");
        return newAppearance;
    }

    default boolean hasAppearance(ItemStack stack) {
        return getAppearance(stack) != Items.AIR;
    }

    default Item getAppearance(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("appearance")) {
            return Registry.ITEM.get(new Identifier(stack.getTag().getString("appearance")));
        }

        return Items.AIR;
    }

    default void setAppearance(ItemStack stack, Item appearance) {
        stack.getOrCreateTag().putString("appearance", Registry.ITEM.getId(appearance).toString());
    }
}
