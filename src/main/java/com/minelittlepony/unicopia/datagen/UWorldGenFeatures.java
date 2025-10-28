package com.minelittlepony.unicopia.datagen;

import java.util.List;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.PineapplePlantFeature;
import com.minelittlepony.unicopia.server.world.UWorldGen;
import com.minelittlepony.unicopia.server.world.gen.UBiomes;
import com.minelittlepony.unicopia.server.world.gen.UCarvers;
import com.minelittlepony.unicopia.server.world.gen.UFeatureConfigs;
import com.minelittlepony.unicopia.server.world.gen.UPlacedFeatures;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.carver.CarverDebugConfig;
import net.minecraft.world.gen.carver.CaveCarverConfig;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.heightprovider.UniformHeightProvider;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.stateprovider.RandomizedIntBlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;

public class UWorldGenFeatures {

    @SuppressWarnings("deprecation")
    static void bootstrapConfiguredCarvers(Registerable<ConfiguredCarver<?>> registerable) {
        registerable.register(UCarvers.OVERWORLD_CLOUD_CARVER_CONFIG, new ConfiguredCarver<>(UWorldGen.CLOUR_CARVER, new CaveCarverConfig(
                0.05F,
                UniformHeightProvider.create(YOffset.fixed(240), YOffset.fixed(600)),
                ConstantFloatProvider.create(0.15F),
                YOffset.aboveBottom(8),
                CarverDebugConfig.DEFAULT,
                RegistryEntryList.of(Blocks.AIR.getRegistryEntry()),
                ConstantFloatProvider.create(3.7F),
                ConstantFloatProvider.create(2.8F),
                ConstantFloatProvider.ZERO
        )));
    }

    static void bootstrapConfiguredFeatures(Registerable<ConfiguredFeature<?, ?>> registerable) {
        registerable.register(UFeatureConfigs.PINEAPPLE_PLANT, new ConfiguredFeature<>(UWorldGen.PINEAPPLE_PLANT_FEATURE, PineapplePlantFeature.Config.INSTANCE));
        registerable.register(UFeatureConfigs.SHELLS, new ConfiguredFeature<>(UWorldGen.SHELLS_FEATURE, ConfiguredFeatures.createRandomPatchFeatureConfig(
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
        )))));
        UTreeFeatures.bootstrapConfiguredFeatures(registerable);
    }

    static void bootstrapPlacedFeatures(Registerable<PlacedFeature> registerable) {
        var configuredFeatures = registerable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        registerable.register(UPlacedFeatures.PINEAPPLE_PLANT, new PlacedFeature(configuredFeatures.getOrThrow(UFeatureConfigs.PINEAPPLE_PLANT), List.of(
            RarityFilterPlacementModifier.of(100),
            SquarePlacementModifier.of(),
            PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
            BiomePlacementModifier.of()
        )));
        registerable.register(UPlacedFeatures.SHELLS, new PlacedFeature(configuredFeatures.getOrThrow(UFeatureConfigs.SHELLS), List.of(
                RarityFilterPlacementModifier.of(1),
                SquarePlacementModifier.of(),
                PlacedFeatures.OCEAN_FLOOR_WG_HEIGHTMAP,
                BiomePlacementModifier.of()
        )));
        UTreeFeatures.bootstrapPlacedFeatures(registerable);
    }

    static void bootstrapBiomes(Registerable<Biome> registerable) {
        registerable.register(UBiomes.SWEET_APPLE_ORCHARD, new Biome.Builder()
                    .precipitation(true)
                    .temperature(0.8F)
                    .downfall(0.8F)
                    .effects(new BiomeEffects.Builder()
                            .waterColor(4159204)
                            .waterFogColor(329011)
                            .fogColor(12638463)
                            .skyColor(OverworldBiomeCreator.getSkyColor(0.8F))
                            .moodSound(BiomeMoodSound.CAVE)
                            .music(MusicType.createIngameMusic(SoundEvents.MUSIC_OVERWORLD_FOREST))
                            .build())
                    .spawnSettings(applyAll(new SpawnSettings.Builder(),
                                DefaultBiomeFeatures::addFarmAnimals,
                                DefaultBiomeFeatures::addBatsAndMonsters
                            ).spawn(SpawnGroup.CREATURE, new SpawnSettings.SpawnEntry(EntityType.WOLF, 5, 4, 4))
                            .build())
                    .generationSettings(applyAll(new GenerationSettings.LookupBackedBuilder(registerable.getRegistryLookup(RegistryKeys.PLACED_FEATURE), registerable.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER)),
                                DefaultBiomeFeatures::addLandCarvers,
                                DefaultBiomeFeatures::addAmethystGeodes,
                                DefaultBiomeFeatures::addDungeons,
                                DefaultBiomeFeatures::addMineables,
                                DefaultBiomeFeatures::addSprings,
                                DefaultBiomeFeatures::addFrozenTopLayer,
                                DefaultBiomeFeatures::addDefaultOres,
                                DefaultBiomeFeatures::addDefaultDisks,
                                DefaultBiomeFeatures::addForestFlowers,
                                DefaultBiomeFeatures::addDefaultFlowers,
                                DefaultBiomeFeatures::addForestGrass,
                                DefaultBiomeFeatures::addDefaultMushrooms,
                                DefaultBiomeFeatures::addDefaultVegetation
                            )
                            .build())
                    .build());
    }

    @SafeVarargs
    static <T> T applyAll(T t, Consumer<T> ...consumers) {
        for (Consumer<T> consumer : consumers) {
            consumer.accept(t);
        }
        return t;
    }

}
