package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.function.Function;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.loottable.BlockLootTableGenerator;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemConvertible;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.TagEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;

public class UBlockAdditionsLootTableProvider extends FabricBlockLootTableProvider {
    public static final LootCondition.Builder WITH_GEM_FINDER = MatchToolLootCondition.builder(ItemPredicate.Builder.create().enchantment(new EnchantmentPredicate(UEnchantments.GEM_FINDER, NumberRange.IntRange.atLeast(1))));

    public static final LootCondition.Builder WITHOUT_SILK_TOUCH_AND_GEM_FINDER = WITHOUT_SILK_TOUCH.and(WITH_GEM_FINDER);
    public static final float[] GEMSTONES_FORTUNE_CHANCE = { 0.1F, 0.14285715F, 0.25F, 1F };

    public static final LootCondition.Builder NEEDS_OCEAN_OR_BEACH_BIOME =
            LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(BiomeKeys.OCEAN))
        .or(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().biome(BiomeKeys.BEACH)));

    public UBlockAdditionsLootTableProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public String getName() {
        return "Block Loot Table Additions";
    }

    @Override
    public void generate() {
        addVanillaDrop(Blocks.STONE, this::gemstoneDrops);
        addVanillaDrop(Blocks.DIRT, block -> gemstoneAndWormDrops(block, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.COARSE_DIRT, block -> gemstoneAndWormDrops(block, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.GRASS_BLOCK, block -> gemstoneAndWormDrops(block, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.GRASS, block -> wormDrops(block, 2, 0.05F, 0.052222223F, 0.055F, 0.066666665F, 0.1F));
        addVanillaDrop(Blocks.MYCELIUM, block -> wormDrops(block, 3, 0.06F, 0.062222223F, 0.065F, 0.077777776F, 0.2F));
        addVanillaDrop(Blocks.PODZOL, block -> wormDrops(block, 4, 0.06F, 0.062222223F, 0.065F, 0.077777776F, 0.2F));
        addVanillaDrop(Blocks.DIAMOND_ORE, this::crystalShardDrops);
        addVanillaDrop(Blocks.DEEPSLATE_DIAMOND_ORE, this::crystalShardDrops);
        addVanillaDrop(Blocks.OAK_LEAVES, block -> chanceDropWithShears(block, UItems.ACORN, GEMSTONES_FORTUNE_CHANCE));
        addVanillaDrop(Blocks.SPRUCE_LEAVES, block -> chanceDropWithShears(block, UItems.PINECONE, GEMSTONES_FORTUNE_CHANCE));
        addVanillaDrop(Blocks.GRAVEL, this::shellDrops);
        addVanillaDrop(Blocks.SUSPICIOUS_GRAVEL, this::shellDrops);
    }

    private void addVanillaDrop(Block block, Function<Block, LootTable.Builder> lootTableFunction) {
        lootTables.put(new Identifier("unicopiamc", block.getLootTableId().getPath()), lootTableFunction.apply(block));
    }

    public LootTable.Builder shellDrops(Block block) {
        return LootTable.builder().pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(WITHOUT_SILK_TOUCH.and(NEEDS_OCEAN_OR_BEACH_BIOME))
                .with(applyExplosionDecay(block, TagEntry.builder(UTags.Items.SHELLS)
                        .apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1)))
                    )
                    .conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, GEMSTONES_FORTUNE_CHANCE)))
                );
    }

    public LootTable.Builder chanceDropWithShears(Block block, ItemConvertible drop, float...chance) {
        return LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(WITHOUT_SILK_TOUCH.and(WITH_SHEARS))
                        .with(chanceDrops(block, drop, 1, chance))
                );
    }

    public LootTable.Builder wormDrops(Block block, int max, float...chance) {
        return LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(WITHOUT_SILK_TOUCH)
                        .with(chanceDrops(block, UItems.WHEAT_WORMS, max, chance))
                );
    }

    public LootTable.Builder gemstoneAndWormDrops(Block block, int max, float...chance) {
        return LootTable.builder()
                .pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(WITHOUT_SILK_TOUCH)
                        .with(gemstoneDrops(block, 0.1F))
                        .with(chanceDrops(block, UItems.WHEAT_WORMS, max, chance))
                );
    }

    public LootTable.Builder gemstoneDrops(Block block) {
        return LootTable.builder()
            .pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(WITHOUT_SILK_TOUCH)
                .with(gemstoneDrops(block, 0.1F))
            );
    }

    public LootTable.Builder crystalShardDrops(Block block) {
        return LootTable.builder()
            .pool(LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(WITHOUT_SILK_TOUCH)
                .with(applyExplosionDecay(block, ItemEntry.builder(UItems.CRYSTAL_SHARD)
                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2)))
                        .apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))
                    )
                    .conditionally(RandomChanceLootCondition.builder(0.25F))
                )
            );
    }

    public LootPoolEntry.Builder<?> gemstoneDrops(Block block, float chance) {
        return applyExplosionDecay(block, ItemEntry.builder(UItems.GEMSTONE)
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2)))
            )
            .conditionally(WITH_GEM_FINDER)
            .conditionally(RandomChanceLootCondition.builder(0.1F))
            .conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, GEMSTONES_FORTUNE_CHANCE));
    }

    public LootPoolEntry.Builder<?> chanceDrops(Block block, ItemConvertible drop, int max, float...chance) {
        return applyExplosionDecay(block, ItemEntry.builder(drop)
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, max)))
            )
            .conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, chance));
    }

    public static LootTable.Builder dropsWithGemfinding(Block drop, LootPoolEntry.Builder<?> child) {
        return BlockLootTableGenerator.drops(drop, WITHOUT_SILK_TOUCH_AND_GEM_FINDER, child);
    }
}
