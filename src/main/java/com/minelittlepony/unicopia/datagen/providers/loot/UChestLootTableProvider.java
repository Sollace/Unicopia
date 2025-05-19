package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.effect.UPotions;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetPotionLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.potion.Potion;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

public class UChestLootTableProvider extends SimpleFabricLootTableProvider {
    public UChestLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup, LootContextTypes.CHEST);
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, Builder> exporter) {
        exporter.accept(RegistryKey.of(RegistryKeys.LOOT_TABLE, Unicopia.id("chests/changeling_hive_trap")), LootTable.builder()
                .pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(6))

                .with(createTippedArrowEntry(UPotions.MORPH_EARTH_PONY.shortEffect(), 3))
                .with(createTippedArrowEntry(UPotions.MORPH_UNICORN.shortEffect(), 1))
                .with(createTippedArrowEntry(UPotions.MORPH_PEGASUS.shortEffect(), 1))
                .with(createTippedArrowEntry(UPotions.MORPH_BAT.shortEffect(), 1))
                .with(createTippedArrowEntry(UPotions.MORPH_KIRIN.shortEffect(), 1))
                .with(createTippedArrowEntry(UPotions.MORPH_HIPPOGRIFF.shortEffect(), 1))

                .with(createTippedArrowEntry(UPotions.MORPH_EARTH_PONY.longEffect(), 5))
                .with(createTippedArrowEntry(UPotions.MORPH_UNICORN.longEffect(), 2))
                .with(createTippedArrowEntry(UPotions.MORPH_PEGASUS.longEffect(), 2))
                .with(createTippedArrowEntry(UPotions.MORPH_BAT.longEffect(), 2))
                .with(createTippedArrowEntry(UPotions.MORPH_KIRIN.longEffect(), 2))
                .with(createTippedArrowEntry(UPotions.MORPH_HIPPOGRIFF.longEffect(), 2))
        ));
    }

    private static ItemEntry.Builder<?> createTippedArrowEntry(RegistryEntry<Potion> potion, int weight) {
        return ItemEntry.builder(Items.TIPPED_ARROW)
                .weight(weight)
                .apply(SetPotionLootFunction.builder(potion))
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(3, 9)));
    }
}
