package com.minelittlepony.unicopia.entity;

import net.minecraft.item.ItemStack;

public interface IFood {

    /**
     * Start eating a piece of food.
     * @param stack
     */
    void begin(ItemStack stack);

    /**
     * Ends eating. Eating was cancelled.
     */
    void end();

    /**
     * Finish eating. *burp*
     */
    void finish();
}
