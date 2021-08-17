package com.minelittlepony.unicopia.item.toxin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface Affliction {
    void afflict(PlayerEntity player, ItemStack stack);

    interface Predicate {
        boolean test(PlayerEntity player, ItemStack stack);
    }
}
