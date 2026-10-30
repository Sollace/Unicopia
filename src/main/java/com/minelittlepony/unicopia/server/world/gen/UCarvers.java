package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.carver.ConfiguredCarver;

public interface UCarvers {
    RegistryKey<ConfiguredCarver<?>> OVERWORLD_CLOUD_CARVER_CONFIG = Unicopia.key(RegistryKeys.CONFIGURED_CARVER, "overworld_cloud_carver");
}
