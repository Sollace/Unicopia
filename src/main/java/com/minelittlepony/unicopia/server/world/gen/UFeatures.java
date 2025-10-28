package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.Feature;

public interface UFeatures {
    RegistryKey<Feature<?>> SHELLS = RegistryKey.of(RegistryKeys.FEATURE, Unicopia.id("shells"));
    RegistryKey<Feature<?>> PINEAPPLE_PLANT = RegistryKey.of(RegistryKeys.FEATURE, Unicopia.id("pineapple_plant"));
}
