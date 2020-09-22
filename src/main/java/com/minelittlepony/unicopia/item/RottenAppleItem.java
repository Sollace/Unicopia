package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.IItemEntity;

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