package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;

public interface UBiomes {
    RegistryKey<Biome> SWEET_APPLE_ORCHARD = of("sweet_apple_orchard");

    static RegistryKey<Biome> of(String name) {
        return RegistryKey.of(RegistryKeys.BIOME, Unicopia.id(name));
    }
}
