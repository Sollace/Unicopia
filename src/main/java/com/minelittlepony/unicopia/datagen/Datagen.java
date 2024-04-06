package com.minelittlepony.unicopia.datagen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.datagen.providers.DietsProvider;
import com.minelittlepony.unicopia.datagen.providers.SeasonsGrowthRatesProvider;
import com.minelittlepony.unicopia.datagen.providers.UAdvancementsProvider;
import com.minelittlepony.unicopia.datagen.providers.UModelProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UBlockAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UBlockLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UChestAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UChestLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UEntityLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.recipe.URecipeProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UBlockTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UDamageTypeProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UItemTagProvider;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.server.world.UWorldGen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.OverworldBiomeCreator;

public class Datagen implements DataGeneratorEntrypoint {
    public static final Logger LOGGER = LogManager.getLogger();

    public static Block getOrCreateBaleBlock(Identifier id) {
        return Registries.BLOCK.getOrEmpty(id).orElseGet(() -> {
            return Registry.register(Registries.BLOCK, id, new EdibleBlock(id, id, false));
        });
    }

    public static Item getOrCreateItem(Identifier id) {
        return Registries.ITEM.getOrEmpty(id).orElseGet(() -> {
            return Registry.register(Registries.ITEM, id, new Item(new Item.Settings()));
        });
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        final var pack = fabricDataGenerator.createPack();
        final var blockTags = pack.addProvider(UBlockTagProvider::new);
        final var itemTags = pack.addProvider((output, registries) -> new UItemTagProvider(output, registries, blockTags));
        pack.addProvider((output, registries) -> new DietsProvider(output, itemTags));
        pack.addProvider(UDamageTypeProvider::new);

        pack.addProvider(UModelProvider::new);
        pack.addProvider(URecipeProvider::new);
        pack.addProvider(UBlockLootTableProvider::new);
        pack.addProvider(UEntityLootTableProvider::new);
        pack.addProvider(UChestLootTableProvider::new);
        pack.addProvider(UBlockAdditionsLootTableProvider::new);
        pack.addProvider(UChestAdditionsLootTableProvider::new);
        pack.addProvider(SeasonsGrowthRatesProvider::new);
        pack.addProvider(UAdvancementsProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder builder) {
        builder.addRegistry(RegistryKeys.BIOME, registerable -> {
            final var placedFeatureLookup = registerable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
            final var carverLookup = registerable.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER);
            registerable.register(UWorldGen.SWEET_APPLE_ORCHARD, OverworldBiomeCreator.createNormalForest(placedFeatureLookup, carverLookup, false, false, false));
        });
        builder.addRegistry(RegistryKeys.DAMAGE_TYPE, UDamageTypes.REGISTRY);
    }
}
