package com.minelittlepony.unicopia.server.world;

import java.util.List;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;

public interface UWorldGen {
    PineapplePlantFeature PINEAPPLE_PLANT_FEATURE = Registry.register(Registries.FEATURE, Unicopia.id("pineapple_plant"), new PineapplePlantFeature());
    RegistryKey<PlacedFeature> PINEAPPLE_PLANT_PLACED_FEATURE = FeatureRegistry.registerPlaceableFeature(Unicopia.id("pineapple_plant"), PINEAPPLE_PLANT_FEATURE, PineapplePlantFeature.Config.INSTANCE, List.of(
        RarityFilterPlacementModifier.of(100),
        SquarePlacementModifier.of(),
        PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
        BiomePlacementModifier.of()
    ));

    static void bootstrap() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, PINEAPPLE_PLANT_PLACED_FEATURE);
        UTreeGen.bootstrap();
    }
}
