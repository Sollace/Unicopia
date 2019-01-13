package com.minelittlepony.unicopia.item;

import net.minecraft.item.ItemStack;

public class ItemRottenApple extends ItemApple {

    public ItemRottenApple(String domain, String name) {
        super(domain, name);
    }

    @Override
    public int getItemBurnTime(ItemStack stack) {
        return 150;
    }
}