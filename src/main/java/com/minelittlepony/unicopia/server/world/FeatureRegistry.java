package com.minelittlepony.unicopia.server.world;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import java.util.function.Function;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.registry.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.registry.entry.*;
import net.minecraft.util.Identifier;

class FeatureRegistry {
    private static final List<ConfiguredEntry> CONFIGURED_FEATURES = new ArrayList<>();
    private static final List<PlacedEntry> PLACED_FEATURES = new ArrayList<>();
    static {
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.getOptional(RegistryKeys.CONFIGURED_FEATURE).ifPresent(registry -> {
                CONFIGURED_FEATURES.forEach(entry -> {
                    Registry.register(registry, entry.key(), entry.factory().get());
                });
            });
            registries.getOptional(RegistryKeys.PLACED_FEATURE).ifPresent(registry -> {
                var lookup = registries.getOptional(RegistryKeys.CONFIGURED_FEATURE).orElseThrow();
                PLACED_FEATURES.forEach(entry -> {
                    Registry.register(registry, entry.key(), entry.factory().apply(lookup.getEntry(entry.configuration().getValue()).orElseThrow()));
                });
            });
        });
    }

    public static <F extends Feature<FC>, FC extends FeatureConfig> RegistryKey<PlacedFeature> registerPlaceableFeature(
            Identifier id,
            F feature, FC featureConfig,
            List<PlacementModifier> placementModifiers) {

        var configurationKey = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id);
        var placementKey = RegistryKey.of(RegistryKeys.PLACED_FEATURE, id);

        CONFIGURED_FEATURES.add(new ConfiguredEntry(configurationKey, () -> new ConfiguredFeature<>(feature, featureConfig)));
        PLACED_FEATURES.add(new PlacedEntry(placementKey, configurationKey, config -> new PlacedFeature(config, placementModifiers)));
        return placementKey;
    }

    record ConfiguredEntry (
        RegistryKey<ConfiguredFeature<?, ?>> key,
        Supplier<ConfiguredFeature<?, ?>> factory
    ) {}

    record PlacedEntry (
            RegistryKey<PlacedFeature> key,
            RegistryKey<ConfiguredFeature<?, ?>> configuration,
            Function<RegistryEntry<ConfiguredFeature<?, ?>>, PlacedFeature> factory
        ) {}
}
