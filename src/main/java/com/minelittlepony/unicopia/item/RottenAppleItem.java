package com.minelittlepony.unicopia.item;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.FoodComponent;

public class RottenAppleItem extends AppleItem {

    public RottenAppleItem(FoodComponent components) {
        super(components);
        FuelRegistry.INSTANCE.add(this, 150);
    }

    @Override
    public boolean onEntityItemUpdate(ItemEntity item) {
        return false;
    }
}