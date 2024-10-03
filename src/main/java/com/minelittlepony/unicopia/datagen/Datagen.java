package com.minelittlepony.unicopia.datagen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.minelittlepony.unicopia.datagen.providers.DietsProvider;
import com.minelittlepony.unicopia.datagen.providers.SeasonsGrowthRatesProvider;
import com.minelittlepony.unicopia.datagen.providers.UAdvancementsProvider;
import com.minelittlepony.unicopia.datagen.providers.UEnchantmentProvider;
import com.minelittlepony.unicopia.datagen.providers.UJukeboxSongProvider;
import com.minelittlepony.unicopia.datagen.providers.UModelProvider;
import com.minelittlepony.unicopia.datagen.providers.UPaintingVariantProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UBlockAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UBlockLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UChestAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UChestLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UEntityAdditionsLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.loot.UEntityLootTableProvider;
import com.minelittlepony.unicopia.datagen.providers.recipe.URecipeProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UBlockTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UDamageTypeProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UDimensionTypeTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UEntityTypeTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UItemTagProvider;
import com.minelittlepony.unicopia.datagen.providers.tag.UStatusEffectTagProvider;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;
import com.minelittlepony.unicopia.server.world.UWorldGen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class Datagen implements DataGeneratorEntrypoint {
    public static final Logger LOGGER = LogManager.getLogger();

    private final UPaintingVariantProvider paintingVariants = new UPaintingVariantProvider();
    private final UEnchantmentProvider enchantments = new UEnchantmentProvider();

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        final var pack = fabricDataGenerator.createPack();
        final var blockTags = pack.addProvider(UBlockTagProvider::new);
        final var itemTags = pack.addProvider((output, registries) -> new UItemTagProvider(output, registries, blockTags));
        pack.addProvider((output, registries) -> new DietsProvider(output, itemTags));
        pack.addProvider(UDamageTypeProvider::new);
        pack.addProvider(UEntityTypeTagProvider::new);
        pack.addProvider(UStatusEffectTagProvider::new);
        pack.addProvider(UDimensionTypeTagProvider::new);

        paintingVariants.addToPack(pack);
        enchantments.addToPack(pack);

        pack.addProvider(UJukeboxSongProvider::new);
        pack.addProvider(UModelProvider::new);
        pack.addProvider(URecipeProvider::new);
        pack.addProvider(UBlockLootTableProvider::new);
        pack.addProvider(UEntityLootTableProvider::new);
        pack.addProvider(UEntityAdditionsLootTableProvider::new);
        pack.addProvider(UChestLootTableProvider::new);
        pack.addProvider(UBlockAdditionsLootTableProvider::new);
        pack.addProvider(UChestAdditionsLootTableProvider::new);
        pack.addProvider(SeasonsGrowthRatesProvider::new);
        pack.addProvider(UAdvancementsProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder builder) {
        builder.addRegistry(RegistryKeys.BIOME, UWorldGen.REGISTRY);
        builder.addRegistry(RegistryKeys.DAMAGE_TYPE, UDamageTypes.REGISTRY);
        builder.addRegistry(RegistryKeys.PAINTING_VARIANT, paintingVariants);
        builder.addRegistry(RegistryKeys.ENCHANTMENT, enchantments);
    }
}
