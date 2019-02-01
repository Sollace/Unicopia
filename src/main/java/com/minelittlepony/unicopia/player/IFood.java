package com.minelittlepony.unicopia.player;

import net.minecraft.item.ItemStack;

public interface IFood {
    void begin(ItemStack stack);

    void end();

    void finish();
}
