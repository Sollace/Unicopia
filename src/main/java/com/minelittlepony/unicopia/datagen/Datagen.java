package com.minelittlepony.unicopia.datagen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.unicopia.datagen.providers.SeasonsGrowthRatesProvider;
import com.minelittlepony.unicopia.datagen.providers.UAdvancementsProvider;
import com.minelittlepony.unicopia.datagen.providers.UBlockTagProvider;
import com.minelittlepony.unicopia.datagen.providers.UItemTagProvider;
import com.minelittlepony.unicopia.datagen.providers.UModelProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UBlockAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UBlockLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UChestAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.recipe.URecipeProvider;
import com.minelittlepony.unicopia.server.world.UWorldGen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

public class Datagen implements DataGeneratorEntrypoint {
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        final FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        UBlockTagProvider blockTags = pack.addProvider(UBlockTagProvider::new);
        pack.addProvider((output, registries) -> new UItemTagProvider(output, registries, blockTags));

        pack.addProvider(UModelProvider::new);
        pack.addProvider(URecipeProvider::new);
        pack.addProvider(UBlockLootTableProvider::new);
        pack.addProvider(UBlockAdditionsLootTableProvider::new);
        pack.addProvider(UChestAdditionsLootTableProvider::new);
        pack.addProvider(SeasonsGrowthRatesProvider::new);
        pack.addProvider(UAdvancementsProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder builder) {
        builder.addRegistry(RegistryKeys.BIOME, registerable -> {
            RegistryEntryLookup<PlacedFeature> placedFeatureLookup = registerable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
            RegistryEntryLookup<ConfiguredCarver<?>> carverLookup = registerable.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER);
            registerable.register(UWorldGen.SWEET_APPLE_ORCHARD, OverworldBiomeCreator.createNormalForest(placedFeatureLookup, carverLookup, false, false, false));
        });
    }
}
