package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.EnchantedCountIncreaseLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

public class UEntityAdditionsLootTableProvider extends SimpleFabricLootTableProvider {
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup;

    public UEntityAdditionsLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup, LootContextTypes.ENTITY);
        this.registryLookup = registryLookup;
    }

    @Override
    public String getName() {
        return super.getName() + " Additions";
    }

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, Builder> exporter) {
        generate(registryLookup.join(), (type, builder) -> exporter.accept(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("unicopiamc", EntityType.getId(type).withPrefixedPath("entities/").getPath())), builder));
    }

    protected void generate(RegistryWrapper.WrapperLookup registryLookup, BiConsumer<EntityType<?>, Builder> exporter) {
        exporter.accept(EntityType.FROG, LootTable.builder()
                .pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(UItems.FROG_LEGS)
                    .apply(EnchantedCountIncreaseLootFunction.builder(registryLookup, ConstantLootNumberProvider.create(2))))));
    }
}
