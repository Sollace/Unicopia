package com.minelittlepony.unicopia.item;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;

public class ItemRottenApple extends AppleItem {

    public ItemRottenApple(FoodComponent components) {
        super(components);
    }

    @Override
    public int getItemBurnTime(ItemStack stack) {
        return 150;
    }

    @Override
    public boolean onEntityItemUpdate(ItemEntity item) {
        return false;
    }
}