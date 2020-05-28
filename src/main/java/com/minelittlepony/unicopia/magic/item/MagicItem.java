package com.minelittlepony.unicopia.magic.item;

import com.minelittlepony.unicopia.magic.Affine;
import com.minelittlepony.unicopia.magic.Affinity;

import net.minecraft.item.ItemStack;

public interface MagicItem extends Affine {
    /**
     * Gets the affinity of this magical artifact. Either good, bad, or unaligned.
     * What this returns may have effects on the behaviour of certain spells and effects.
     */
    default Affinity getAffinity(ItemStack stack) {
        return getAffinity();
    }
}
