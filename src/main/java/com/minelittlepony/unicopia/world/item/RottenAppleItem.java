package com.minelittlepony.unicopia.world.item;

import com.minelittlepony.unicopia.ducks.IItemEntity;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.util.ActionResult;

public class RottenAppleItem extends AppleItem {

    public RottenAppleItem(Settings settings) {
        super(settings);
        FuelRegistry.INSTANCE.add(this, 150);
    }

    @Override
    public ActionResult onGroundTick(IItemEntity item) {
        return ActionResult.PASS;
    }
}