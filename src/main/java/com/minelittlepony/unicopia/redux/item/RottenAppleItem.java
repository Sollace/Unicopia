package com.minelittlepony.unicopia.redux.item;

import com.minelittlepony.unicopia.core.ducks.IItemEntity;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.FoodComponent;
import net.minecraft.util.ActionResult;

public class RottenAppleItem extends AppleItem {

    public RottenAppleItem(FoodComponent components) {
        super(components);
        FuelRegistry.INSTANCE.add(this, 150);
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        return ActionResult.PASS;
    }
}