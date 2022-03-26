package com.minelittlepony.unicopia.item.toxin;

import java.util.Optional;

import net.minecraft.item.ItemStack;

public interface ToxicHolder {
    default Optional<Toxic> getToxic(ItemStack stack) {
        return Optional.empty();
    }
}
