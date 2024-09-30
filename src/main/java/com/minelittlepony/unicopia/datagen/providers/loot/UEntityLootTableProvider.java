package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.TagEntry;
import net.minecraft.loot.function.EnchantedCountIncreaseLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

public class UEntityLootTableProvider extends SimpleFabricLootTableProvider {
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup;

    public UEntityLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup, LootContextTypes.ENTITY);
        this.registryLookup = registryLookup;
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, Builder> exporter) {
        generate(registryLookup.join(), (type, builder) -> exporter.accept(RegistryKey.of(RegistryKeys.LOOT_TABLE, EntityType.getId(type).withPrefixedPath("entities/")), builder));
    }

    protected void generate(RegistryWrapper.WrapperLookup registryLookup, BiConsumer<EntityType<?>, Builder> exporter) {
        exporter.accept(UEntities.BUTTERFLY, LootTable.builder()
                .pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(UItems.BUTTERFLY)
                    .apply(EnchantedCountIncreaseLootFunction.builder(registryLookup, UniformLootNumberProvider.create(0, 1))))));
        exporter.accept(UEntities.STORM_CLOUD, LootTable.builder()
                .pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(UItems.CLOUD_LUMP)
                    .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(6, 12)))
                    .apply(EnchantedCountIncreaseLootFunction.builder(registryLookup, UniformLootNumberProvider.create(0, 1))))));
        exporter.accept(UEntities.LOOT_BUG, LootTable.builder()
                .pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(TagEntry.builder(UTags.Items.LOOT_BUG_COMMON_DROPS)
                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(6, 12)))
                        .apply(EnchantedCountIncreaseLootFunction.builder(registryLookup, UniformLootNumberProvider.create(0, 3))))
                .with(TagEntry.builder(UTags.Items.LOOT_BUG_RARE_DROPS)
                        .conditionally(RandomChanceLootCondition.builder(0.25F))
                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 3)))
                        .apply(EnchantedCountIncreaseLootFunction.builder(registryLookup, UniformLootNumberProvider.create(0, 6))))
                .with(TagEntry.builder(UTags.Items.LOOT_BUG_EPIC_DROPS)
                        .conditionally(RandomChanceLootCondition.builder(0.1F))
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1)))
                        .apply(EnchantedCountIncreaseLootFunction.builder(registryLookup, UniformLootNumberProvider.create(0, 2))))
        ));
    }
}
