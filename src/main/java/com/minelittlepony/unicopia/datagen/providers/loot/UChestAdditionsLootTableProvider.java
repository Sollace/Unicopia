package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.TagEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class UChestAdditionsLootTableProvider extends SimpleFabricLootTableProvider {

    public UChestAdditionsLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup, LootContextTypes.CHEST);
    }

    @Override
    public String getName() {
        return super.getName() + " Additions";
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> exporter) {
        acceptAdditions((id, builder) -> exporter.accept(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("unicopiamc", id.getValue().getPath())), builder));
    }

    public void acceptAdditions(BiConsumer<RegistryKey<LootTable>, Builder> exporter) {
        exporter.accept(LootTables.ABANDONED_MINESHAFT_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(2, 4))
                .with(ItemEntry.builder(UItems.GRYPHON_FEATHER).weight(2).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 4))))
        ));
        exporter.accept(LootTables.WOODLAND_MANSION_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(2, 4))
                .with(ItemEntry.builder(UItems.GRYPHON_FEATHER).weight(10).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 7))))
                .with(ItemEntry.builder(UItems.GOLDEN_WING).weight(1).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                .with(TagEntry.expandBuilder(UTags.Items.FRESH_APPLES).weight(1).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2, 5))))
        ));
        exporter.accept(LootTables.VILLAGE_FLETCHER_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(2, 4))
                .with(ItemEntry.builder(UItems.GRYPHON_FEATHER).weight(10).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
                .with(ItemEntry.builder(UItems.PEGASUS_FEATHER).weight(1).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2))))
        ));
        exporter.accept(LootTables.VILLAGE_PLAINS_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(3, 4))
                .with(TagEntry.expandBuilder(UTags.Items.FRESH_APPLES).weight(1))
                .with(TagEntry.expandBuilder(UTags.Items.APPLE_SEEDS).weight(1))
        ));

        exporter.accept(LootTables.ANCIENT_CITY_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(0, 1))
                .with(ItemEntry.builder(UItems.GROGARS_BELL).weight(1))
        ));

        exporter.accept(LootTables.BURIED_TREASURE_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 4))
                .with(ItemEntry.builder(UItems.PEARL_NECKLACE).weight(1))
                .with(TagEntry.expandBuilder(UTags.Items.SHELLS).weight(3))
        ));
        exporter.accept(LootTables.SHIPWRECK_SUPPLY_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 6))
                .with(TagEntry.expandBuilder(UTags.Items.SHELLS).weight(3))
                .with(TagEntry.expandBuilder(UConventionalTags.Items.ROTTEN_FISH).weight(1))
        ));
        exporter.accept(LootTables.SHIPWRECK_TREASURE_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 4))
                .with(ItemEntry.builder(UItems.PEARL_NECKLACE).weight(1))
                .with(TagEntry.expandBuilder(UTags.Items.SHELLS).weight(3))
                .with(TagEntry.expandBuilder(UConventionalTags.Items.ROTTEN_FISH).weight(1))
        ));
        exporter.accept(LootTables.UNDERWATER_RUIN_BIG_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 2))
                .with(ItemEntry.builder(UItems.PEARL_NECKLACE).weight(1))
                .with(ItemEntry.builder(UItems.SHELLY).weight(4))
                .with(TagEntry.expandBuilder(UTags.Items.SHELLS).weight(8))
                .with(TagEntry.expandBuilder(UConventionalTags.Items.ROTTEN_FISH).weight(1))
        ));
        exporter.accept(LootTables.UNDERWATER_RUIN_SMALL_CHEST, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 4))
                .with(TagEntry.expandBuilder(UTags.Items.SHELLS).weight(1))
                .with(TagEntry.expandBuilder(UConventionalTags.Items.ROTTEN_FISH).weight(1))
        ));

        exporter.accept(LootTables.DESERT_WELL_ARCHAEOLOGY, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 4))
                .with(ItemEntry.builder(UItems.WEIRD_ROCK).weight(2))
                .with(ItemEntry.builder(UItems.ROCK).weight(1))
                .with(ItemEntry.builder(UItems.TOM).weight(1))
                .with(ItemEntry.builder(UItems.ROCK_STEW).weight(1))
                .with(ItemEntry.builder(UItems.PEBBLES).weight(1))
                .with(ItemEntry.builder(UItems.SHELLY).weight(1))
                .with(TagEntry.expandBuilder(UTags.Items.SHELLS).weight(1))
                .with(ItemEntry.builder(UItems.PEARL_NECKLACE).weight(1))
        ));
        exporter.accept(LootTables.TRAIL_RUINS_COMMON_ARCHAEOLOGY, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 4))
                .with(ItemEntry.builder(UItems.MEADOWBROOKS_STAFF).weight(2))
                .with(ItemEntry.builder(UItems.BOTCHED_GEM).weight(3))
                .with(ItemEntry.builder(UItems.PEGASUS_FEATHER).weight(1))
        ));
        exporter.accept(LootTables.TRAIL_RUINS_RARE_ARCHAEOLOGY, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 4))
                .with(ItemEntry.builder(UItems.BROKEN_SUNGLASSES).weight(2))
                .with(ItemEntry.builder(UItems.EMPTY_JAR).weight(2))
                .with(ItemEntry.builder(UItems.MUSIC_DISC_CRUSADE).weight(1))
        ));
        exporter.accept(LootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 2))
                .with(TagEntry.expandBuilder(UTags.Items.SHELLS).weight(2))
                .with(ItemEntry.builder(UItems.PEARL_NECKLACE).weight(1))
                .with(TagEntry.expandBuilder(UConventionalTags.Items.ROTTEN_FISH).weight(2))
        ));

        exporter.accept(LootTables.FISHING_GAMEPLAY, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 2))
                .with(TagEntry.expandBuilder(UTags.Items.SHELLS).weight(2))
                .with(TagEntry.expandBuilder(UConventionalTags.Items.ROTTEN_FISH).weight(1))
        ));

        exporter.accept(LootTables.FISHING_JUNK_GAMEPLAY, LootTable.builder().pool(LootPool.builder()
                .rolls(UniformLootNumberProvider.create(1, 2))
                .with(ItemEntry.builder(UItems.BROKEN_SUNGLASSES).weight(2))
                .with(ItemEntry.builder(UItems.WHEAT_WORMS).weight(2))
                .with(TagEntry.expandBuilder(UConventionalTags.Items.ROTTEN_FISH).weight(1))
                .with(ItemEntry.builder(UItems.BOTCHED_GEM).weight(4))
        ));

        exporter.accept(LootTables.HERO_OF_THE_VILLAGE_FISHERMAN_GIFT_GAMEPLAY, LootTable.builder().pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(UItems.PEARL_NECKLACE).weight(1))
                .with(ItemEntry.builder(UItems.SHELLY).weight(1))
        ));
    }

}
