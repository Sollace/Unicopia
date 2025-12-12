package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.server.world.gen.CaveCarvingStructureProcessor;
import com.minelittlepony.unicopia.server.world.gen.CloudCarver;
import com.minelittlepony.unicopia.server.world.gen.FruitBlobFoliagePlacer;
import com.minelittlepony.unicopia.server.world.gen.StructureExtensions;
import com.minelittlepony.unicopia.server.world.gen.OverworldBiomeSelectionCallback;
import com.minelittlepony.unicopia.server.world.gen.SurfaceGrowthStructureProcessor;
import com.minelittlepony.unicopia.server.world.gen.UBiomes;
import com.minelittlepony.unicopia.server.world.gen.UCarvers;
import com.minelittlepony.unicopia.server.world.gen.UFeatures;
import com.minelittlepony.unicopia.server.world.gen.UPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.CaveCarverConfig;
import net.minecraft.world.gen.feature.RandomPatchFeature;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

public interface UWorldGen {
    PineapplePlantFeature PINEAPPLE_PLANT_FEATURE = Registry.register(Registries.FEATURE, UFeatures.PINEAPPLE_PLANT, new PineapplePlantFeature());
    RandomPatchFeature SHELLS_FEATURE = Registry.register(Registries.FEATURE, UFeatures.SHELLS, new RandomPatchFeature(RandomPatchFeatureConfig.CODEC));

    StructureProcessorType<SurfaceGrowthStructureProcessor> SURFACE_GROWTH_STRUCTURE_PROCESSOR = Registry.register(Registries.STRUCTURE_PROCESSOR, Unicopia.id("surface_growth"), () -> SurfaceGrowthStructureProcessor.CODEC);
    StructureProcessorType<CaveCarvingStructureProcessor> CAVE_CARVING_STRUCTURE_PROCESSOR = Registry.register(Registries.STRUCTURE_PROCESSOR, Unicopia.id("cave_carving"), () -> CaveCarvingStructureProcessor.CODEC);

    Carver<CaveCarverConfig> CLOUR_CARVER = Registry.register(Registries.CARVER, Unicopia.id("cloud"), new CloudCarver(CaveCarverConfig.CAVE_CODEC));

    FoliagePlacerType<FernFoliagePlacer> FERN_FOLIAGE_PLACER_TYPE = Registry.register(Registries.FOLIAGE_PLACER_TYPE, Unicopia.id("fern_foliage"), new FoliagePlacerType<>(FernFoliagePlacer.CODEC));
    FoliagePlacerType<FruitBlobFoliagePlacer> FRUIT_BLOB_FOLIAGE_PLACER_TYPE = Registry.register(Registries.FOLIAGE_PLACER_TYPE, Unicopia.id("fruit_blob_foliage"), new FoliagePlacerType<>(FruitBlobFoliagePlacer.CODEC));

    static void bootstrap() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_JUNGLE), GenerationStep.Feature.VEGETAL_DECORATION, UPlacedFeatures.PINEAPPLE_PLANT);
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OCEAN)
                .or(BiomeSelectors.tag(BiomeTags.IS_DEEP_OCEAN)
                .or(BiomeSelectors.tag(BiomeTags.IS_RIVER))
                .or(BiomeSelectors.includeByKey(BiomeKeys.STONY_SHORE))
        ), GenerationStep.Feature.VEGETAL_DECORATION, UPlacedFeatures.SHELLS);
        BiomeModifications.addCarver(BiomeSelectors.foundInOverworld(), GenerationStep.Carver.AIR, UCarvers.OVERWORLD_CLOUD_CARVER_CONFIG);
        UTreeGen.bootstrap();

        OverworldBiomeSelectionCallback.EVENT.register(context -> {
            if (context.biomeKey() == BiomeKeys.FOREST) {
                context.addOverride(context.referenceFrame().temperature().splitAbove(0.9F), UBiomes.SWEET_APPLE_ORCHARD);
            }
        });
        StructureExtensions.bootstrap();
    }
}
