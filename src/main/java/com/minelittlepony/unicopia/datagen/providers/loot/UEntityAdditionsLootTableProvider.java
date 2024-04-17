package com.minelittlepony.unicopia.datagen.providers.loot;

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
import net.minecraft.loot.function.LootingEnchantLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

public class UEntityAdditionsLootTableProvider extends SimpleFabricLootTableProvider {
    public UEntityAdditionsLootTableProvider(FabricDataOutput output) {
        super(output, LootContextTypes.ENTITY);
    }

    @Override
    public String getName() {
        return super.getName() + " Additions";
    }

    @Override
    public void accept(BiConsumer<Identifier, Builder> exporter) {
        generate((type, builder) -> exporter.accept(new Identifier("unicopiamc", EntityType.getId(type).withPrefixedPath("entities/").getPath()), builder));
    }

    protected void generate(BiConsumer<EntityType<?>, Builder> exporter) {
        exporter.accept(EntityType.FROG, LootTable.builder()
                .pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(UItems.FROG_LEGS)
                    .apply(LootingEnchantLootFunction.builder(ConstantLootNumberProvider.create(2))))));
    }
}
