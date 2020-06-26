package com.minelittlepony.unicopia.magic;

import com.minelittlepony.unicopia.equine.player.Pony;

import net.minecraft.item.ItemStack;

public interface AffineItem extends Affine {
    /**
     * Gets the affinity of this magical artifact. Either good, bad, or unaligned.
     * What this returns may have effects on the behaviour of certain spells and effects.
     */
    default Affinity getAffinity(ItemStack stack) {
        return getAffinity();
    }

    default void onRemoved(Pony player, float needfulness) {

    }

    boolean alwaysActive();
}
