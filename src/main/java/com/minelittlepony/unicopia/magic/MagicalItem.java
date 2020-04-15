package com.minelittlepony.unicopia.magic;

import net.minecraft.item.ItemStack;

public interface MagicalItem extends Affine {
    /**
     * Gets the affinity of this magical artifact. Either good, bad, or unaligned.
     * What this returns may have effects on the behaviour of certain spells and effects.
     */
    default Affinity getAffinity(ItemStack stack) {
        return getAffinity();
    }
}
