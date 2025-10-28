package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public interface UFeatureConfigs {
    RegistryKey<ConfiguredFeature<?, ?>> PINEAPPLE_PLANT = of("pineapple_plant");
    RegistryKey<ConfiguredFeature<?, ?>> SHELLS = of("shells");

    RegistryKey<ConfiguredFeature<?, ?>> ZAP_APPLE_TREE = of("zap_apple_tree");
    RegistryKey<ConfiguredFeature<?, ?>> GREEN_APPLE_TREE = of("green_apple_tree");
    RegistryKey<ConfiguredFeature<?, ?>> SWEET_APPLE_TREE = of("sweet_apple_tree");
    RegistryKey<ConfiguredFeature<?, ?>> SOUR_APPLE_TREE = of("sour_apple_tree");
    RegistryKey<ConfiguredFeature<?, ?>> GOLDEN_OAK_TREE = of("golden_oak_tree");
    RegistryKey<ConfiguredFeature<?, ?>> BANANA_TREE = of("banana_tree");
    RegistryKey<ConfiguredFeature<?, ?>> MANGO_TREE = of("mango_tree");

    static RegistryKey<ConfiguredFeature<?, ?>> of(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Unicopia.id(name));
    }
}
