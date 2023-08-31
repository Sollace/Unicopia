package com.minelittlepony.unicopia.item;

import net.minecraft.item.ItemStack;

public interface ChargeableItem {

    int getMaxCharge();

    default int getDefaultCharge() {
        return 0;
    }

    default boolean isChargable() {
        return getMaxCharge() > 0;
    }

    default boolean hasCharge(ItemStack stack) {
        return getEnergy(stack) > 0;
    }

    default ItemStack recharge(ItemStack stack) {
        return setEnergy(stack, getMaxCharge());
    }

    default ItemStack recharge(ItemStack stack, float amount) {
        return setEnergy(stack, getEnergy(stack) + amount);
    }

    default boolean canCharge(ItemStack stack) {
        return isChargable() && getEnergy(stack) < getMaxCharge();
    }

    default float getChargeRemainder(ItemStack stack) {
        return Math.max(0, getMaxCharge() - getEnergy(stack));
    }

    default void onDischarge(ItemStack stack) {

    }

    static void consumeEnergy(ItemStack stack, float amount) {
        setEnergy(stack, getEnergy(stack) - amount);
        if (stack.getItem() instanceof ChargeableItem c) {
            c.onDischarge(stack);
        }
    }

    static int getDefaultCharge(ItemStack stack) {
        return stack.getItem() instanceof ChargeableItem c ? c.getDefaultCharge() : 0;
    }

    static float getEnergy(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains("energy") ? Math.max(0, stack.getNbt().getFloat("energy")) : getDefaultCharge(stack);
    }

    static ItemStack setEnergy(ItemStack stack, float energy) {
        if (energy <= 0 && getDefaultCharge(stack) <= 0) {
            stack.removeSubNbt("energy");
        } else {
            stack.getOrCreateNbt().putFloat("energy", energy);
        }
        return stack;
    }
}
