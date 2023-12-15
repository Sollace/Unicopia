package com.minelittlepony.unicopia;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.world.poi.PointOfInterestType;

public interface UPOIs {
    PointOfInterestType CHESTS = PointOfInterestHelper.register(Unicopia.id("chests"), 1, 64, Registries.BLOCK.getEntrySet().stream()
            .map(entry -> entry.getValue())
            .filter(b -> b instanceof AbstractChestBlock)
            .toArray(Block[]::new));

    static void bootstrap() { }
}
