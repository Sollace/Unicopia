package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.Map;
import java.util.function.Function;

import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.IndirectionUtils;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.block.Block;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.entry.DynamicEntry;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;

public class UExternalBlockLootTableProvider {
    private final Map<RegistryKey<LootTable>, LootTable.Builder> lootTables;
    private final ResourceCondition[] conditions;

    protected UExternalBlockLootTableProvider(Map<RegistryKey<LootTable>, LootTable.Builder> lootTables, ResourceCondition...conditions) {
        this.lootTables = lootTables;
        this.conditions = conditions;
    }

    public void addDrop(Identifier block, Function<Identifier, LootTable.Builder> drop) {
        RegistryKey<LootTable> key = RegistryKey.of(RegistryKeys.LOOT_TABLE, block.withPrefixedPath("blocks/"));
        LootTable.Builder table = drop.apply(block);
        FabricDataGenHelper.addConditions(table, conditions);
        lootTables.put(key, table);
    }

    public static LootTable.Builder edibleBlockDrops(Identifier block, Identifier drop) {
        LootTable.Builder builder = LootTable.builder();
        for (BooleanProperty segment : EdibleBlock.SEGMENTS) {
            builder
                .pool(LootPool.builder()
                    .rolls(UBlockLootTableProvider.exactly(1))
                        .with(applyStateCondition(block, segment, true, DynamicEntry.builder(drop)))
                        .conditionally(SurvivesExplosionLootCondition.builder()));
        }
        return builder;
    }

    public static <T extends LootConditionConsumingBuilder<T>> T applyStateCondition(Identifier block,
            BooleanProperty property, boolean value, LootConditionConsumingBuilder<T> builder) {
        RegistryEntry<Block> entry = IndirectionUtils.entryOf(RegistryKeys.BLOCK, block, UBlocks.HAY_BLOCK);
        return builder.conditionally(() -> new BlockStatePropertyLootCondition(
                entry,
                StatePredicate.Builder.create().exactMatch(property, value).build())
        );
    }
}
