package com.minelittlepony.unicopia.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemRottenApple extends ItemApple {

    public ItemRottenApple(String domain, String name) {
        super(domain, name);
    }

    @Override
    public int getItemBurnTime(ItemStack stack) {
        return 150;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
        }
    }
}