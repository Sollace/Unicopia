package com.minelittlepony.unicopia.item;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.Item;

public class RottenAppleItem extends Item {

    public RottenAppleItem(Settings settings) {
        super(settings);
        FuelRegistry.INSTANCE.add(this, 150);
    }
}