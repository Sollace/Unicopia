package com.minelittlepony.unicopia.edibles;

import net.minecraft.item.ItemStack;

public class CookedToxicityDeterminent implements IEdible {
    public static final IEdible instance = new CookedToxicityDeterminent();

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        return Toxicity.byMetadata(stack.getMetadata());
    }
}
