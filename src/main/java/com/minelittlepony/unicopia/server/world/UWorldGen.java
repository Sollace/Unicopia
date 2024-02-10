package com.minelittlepony.unicopia.server.world;

import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.gen.OverworldBiomeSelectionCallback;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.RandomPatchFeature;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.RandomizedIntBlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;

public interface UWorldGen {
    PineapplePlantFeature PINEAPPLE_PLANT_FEATURE = Registry.register(Registries.FEATURE, Unicopia.id("pineapple_plant"), new PineapplePlantFeature());
    RegistryKey<PlacedFeature> PINEAPPLE_PLANT_PLACED_FEATURE = FeatureRegistry.registerPlaceableFeature(Unicopia.id("pineapple_plant"), PINEAPPLE_PLANT_FEATURE, PineapplePlantFeature.Config.INSTANCE, List.of(
        RarityFilterPlacementModifier.of(100),
        SquarePlacementModifier.of(),
        PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
        BiomePlacementModifier.of()
    ));

    RandomPatchFeature SHELLS_FEATURE = Registry.register(Registries.FEATURE, Unicopia.id("shells"), new RandomPatchFeature(RandomPatchFeatureConfig.CODEC));
    RegistryKey<PlacedFeature> SHELLS_PLACED_FEATURE = FeatureRegistry.registerPlaceableFeature(Unicopia.id("shells"), SHELLS_FEATURE, ConfiguredFeatures.createRandomPatchFeatureConfig(
            25,
            PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(
            new RandomizedIntBlockStateProvider(
                new WeightedBlockStateProvider(DataPool.<BlockState>builder()
                        .add(UBlocks.CLAM_SHELL.getDefaultState(), 1)
                        .add(UBlocks.SCALLOP_SHELL.getDefaultState(), 2)
                        .add(UBlocks.TURRET_SHELL.getDefaultState(), 2)
                ),
                ShellsBlock.COUNT,
                UniformIntProvider.create(1, 4)
            )
        ), BlockPredicate.allOf(
                BlockPredicate.matchingBlocks(Blocks.WATER),
                BlockPredicate.hasSturdyFace(new Vec3i(0, -1, 0), Direction.UP)
        ))), List.of(
            RarityFilterPlacementModifier.of(1),
            SquarePlacementModifier.of(),
            PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP,
            BiomePlacementModifier.of()
    ));

    RegistryKey<Biome> SWEET_APPLE_ORCHARD = RegistryKey.of(RegistryKeys.BIOME, Unicopia.id("sweet_apple_orchard"));

    static void bootstrap() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, PINEAPPLE_PLANT_PLACED_FEATURE);
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OCEAN)
                .or(BiomeSelectors.tag(BiomeTags.IS_DEEP_OCEAN)
                .or(BiomeSelectors.tag(BiomeTags.IS_RIVER))
                .or(BiomeSelectors.includeByKey(BiomeKeys.STONY_SHORE))
        ), GenerationStep.Feature.VEGETAL_DECORATION, SHELLS_PLACED_FEATURE);
        UTreeGen.bootstrap();

        OverworldBiomeSelectionCallback.EVENT.register(context -> {
            if (context.biomeKey() == BiomeKeys.FOREST) {
                context.addOverride(context.referenceFrame().temperature().splitAbove(0.9F), SWEET_APPLE_ORCHARD);
            }
        });
    }
}
