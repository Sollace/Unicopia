package com.minelittlepony.unicopia.server.world.gen;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.feature.PlacedFeature;

public interface UPlacedFeatures {
    RegistryKey<PlacedFeature> SHELLS = of("shells");
    RegistryKey<PlacedFeature> PINEAPPLE_PLANT = of("pineapple_plant");

    RegistryKey<PlacedFeature> ZAP_APPLE_TREE = of("zap_apple_tree_placed");
    RegistryKey<PlacedFeature> GREEN_APPLE_TREE = of("green_apple_tree_placed");
    RegistryKey<PlacedFeature> SWEET_APPLE_TREE = of("sweet_apple_tree_placed");
    RegistryKey<PlacedFeature> SWEET_APPLE_TREE_ORCHARD = of("sweet_apple_tree_orchard_placed");
    RegistryKey<PlacedFeature> SOUR_APPLE_TREE = of("sour_apple_tree_placed");
    RegistryKey<PlacedFeature> BANANA_TREE = of("banana_tree_placed");
    RegistryKey<PlacedFeature> MANGO_TREE = of("mango_tree_placed");

    static RegistryKey<PlacedFeature> of(String id) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Unicopia.id(id));
    }
}
