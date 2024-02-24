package com.minelittlepony.unicopia.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

@Mixin(PointOfInterestTypes.class)
public interface PointOfInterestTypesAccessor {
    @Invoker("registerStates")
    static void registerStates(RegistryEntry<PointOfInterestType> poiTypeEntry, Set<BlockState> states) {

    }
}
