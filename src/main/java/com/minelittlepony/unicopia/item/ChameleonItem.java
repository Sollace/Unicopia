package com.minelittlepony.unicopia.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

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
        ItemStack newAppearance = appearance.getDefaultStack();
        if (stack.hasNbt()) {
            newAppearance.setNbt(stack.getNbt().copy());
        }
        newAppearance.setCount(stack.getCount());
        newAppearance.removeSubNbt("appearance");
        return newAppearance;
    }

    default boolean hasAppearance(ItemStack stack) {
        return getAppearance(stack) != Items.AIR;
    }

    default Item getAppearance(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("appearance")) {
            return Registries.ITEM.get(new Identifier(stack.getNbt().getString("appearance")));
        }

        return Items.AIR;
    }

    default ItemStack setAppearance(ItemStack stack, ItemStack appearance) {
        ItemStack result = stack.copy();

        if (appearance.hasNbt()) {
            result.setNbt(appearance.getNbt().copy());
            result.removeCustomName();
            result.setDamage(stack.getDamage());
            result.setCount(stack.getCount());
        }
        result.getOrCreateNbt().putString("appearance", Registries.ITEM.getId(appearance.getItem()).toString());

        return result;
    }
}
