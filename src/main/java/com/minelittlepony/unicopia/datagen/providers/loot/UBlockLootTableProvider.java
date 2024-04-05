package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.block.EnchantedFruitBlock;
import com.minelittlepony.unicopia.block.PieBlock;
import com.minelittlepony.unicopia.block.PileBlock;
import com.minelittlepony.unicopia.block.SegmentedCropBlock;
import com.minelittlepony.unicopia.block.ShellsBlock;
import com.minelittlepony.unicopia.block.SlimePustuleBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.minelittlepony.unicopia.datagen.providers.UModelProvider;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.Tree;
import com.minelittlepony.unicopia.server.world.UTreeGen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarrotsBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;

public class UBlockLootTableProvider extends FabricBlockLootTableProvider {

    private static final ConditionalLootFunction.Builder<?> FORTUNE_BONUS = ApplyBonusLootFunction.binomialWithBonusCount(Enchantments.FORTUNE, 0.5714286F, 3);

    public UBlockLootTableProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        // simple drops
        List.of(
            UBlocks.CARVED_CLOUD, UBlocks.UNSTABLE_CLOUD,
            UBlocks.CHISELLED_CHITIN_STAIRS, UBlocks.CHISELLED_CHITIN,
            UBlocks.CLOUD_BRICK_STAIRS, UBlocks.CLOUD_BRICKS,
            UBlocks.CLOUD_PLANK_STAIRS, UBlocks.CLOUD_PLANKS,
            UBlocks.CURING_JOKE,
            UBlocks.GOLDEN_OAK_LOG,
            UBlocks.HIVE,

            UBlocks.PALM_BUTTON, UBlocks.PALM_FENCE_GATE, UBlocks.PALM_FENCE, UBlocks.PALM_LOG, UBlocks.PALM_PLANKS,
            UBlocks.PALM_PRESSURE_PLATE, UBlocks.PALM_SIGN, UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_STAIRS, UBlocks.PALM_TRAPDOOR, UBlocks.PALM_WOOD,

            UBlocks.STRIPPED_PALM_LOG, UBlocks.STRIPPED_PALM_WOOD,
            UBlocks.STRIPPED_ZAP_LOG, UBlocks.STRIPPED_ZAP_WOOD,

            UBlocks.WAXED_STRIPPED_ZAP_LOG, UBlocks.WAXED_STRIPPED_ZAP_WOOD,
            UBlocks.WAXED_ZAP_FENCE_GATE, UBlocks.WAXED_ZAP_FENCE,
            UBlocks.WAXED_ZAP_LOG, UBlocks.WAXED_ZAP_PLANKS, UBlocks.WAXED_ZAP_STAIRS, UBlocks.WAXED_ZAP_WOOD,

            UBlocks.WEATHER_VANE,

            UBlocks.ZAP_FENCE_GATE, UBlocks.ZAP_FENCE,
            UBlocks.ZAP_LOG, UBlocks.ZAP_PLANKS, UBlocks.ZAP_STAIRS, UBlocks.ZAP_WOOD,
            UBlocks.WORM_BLOCK
        ).forEach(this::addDrop);

        // slabs
        List.of(
                UBlocks.CHISELLED_CHITIN_SLAB, UBlocks.CLOUD_BRICK_SLAB,
                UBlocks.CLOUD_PLANK_SLAB, UBlocks.PALM_SLAB, UBlocks.ZAP_SLAB, UBlocks.WAXED_ZAP_SLAB
        ).forEach(slab -> addDrop(slab, this::slabDrops));
        addDrop(UBlocks.CLOUD_SLAB, slab -> decomposingSlabDrops(slab, UItems.CLOUD_LUMP, 2));
        addDrop(UBlocks.SOGGY_CLOUD_SLAB, slab -> decomposingSlabDrops(slab, UItems.CLOUD_LUMP, 2));
        addDrop(UBlocks.DENSE_CLOUD_SLAB, slab -> decomposingSlabDrops(slab, UItems.CLOUD_LUMP, 4));
        addDrop(UBlocks.ETCHED_CLOUD_SLAB, slab -> decomposingSlabDrops(slab, UItems.CLOUD_LUMP, 4));

        // fruit
        UModelProvider.FRUITS.forEach((block, drop) -> {
            if (block != UBlocks.GOLDEN_APPLE) {
                addDrop(block, fortuneBonusDrops(drop));
            }
        });
        addDrop(UBlocks.GOLDEN_APPLE, LootTable.builder().pool(LootPool.builder()
            .rolls(exactly(1))
            .with(applyStateCondition(UBlocks.GOLDEN_APPLE, EnchantedFruitBlock.ENCHANTED, false, applyExplosionDecay(UBlocks.GOLDEN_APPLE, ItemEntry.builder(Items.GOLDEN_APPLE))).apply(FORTUNE_BONUS))
            .with(applyStateCondition(UBlocks.GOLDEN_APPLE, EnchantedFruitBlock.ENCHANTED, true, applyExplosionDecay(UBlocks.GOLDEN_APPLE, ItemEntry.builder(Items.ENCHANTED_GOLDEN_APPLE))).apply(FORTUNE_BONUS))
        ));
        List.of(UBlocks.GREEN_APPLE_LEAVES, UBlocks.SOUR_APPLE_LEAVES, UBlocks.SWEET_APPLE_LEAVES, UBlocks.GOLDEN_OAK_LEAVES).forEach(block -> addDrop(block, this::fruitLeavesDrops));
        addDrop(UBlocks.MANGO_LEAVES, block -> leavesDrops(block, UTreeGen.MANGO_TREE.sapling().get(), 0.025F, 0.027777778F, 0.03125F, 0.041666668F, 0.1F)); // same chance as jungle
        addDrop(UBlocks.ZAP_LEAVES, block -> leavesDrops(block, UTreeGen.ZAP_APPLE_TREE.sapling().get(), SAPLING_DROP_CHANCE));
        addDrop(UBlocks.FLOWERING_ZAP_LEAVES, block -> leavesDrops(block, UTreeGen.ZAP_APPLE_TREE.sapling().get(), SAPLING_DROP_CHANCE));
        addDrop(UBlocks.PALM_LEAVES, block -> leavesDrops(block, UTreeGen.BANANA_TREE.sapling().get(), SAPLING_DROP_CHANCE));

        Tree.REGISTRY.forEach(tree -> {
            tree.sapling().ifPresent(this::addDrop);
            tree.pot().ifPresent(this::addPottedPlantDrops);
        });

        // jars
        List.of(
                UBlocks.JAR, UBlocks.CLOUD_JAR, UBlocks.STORM_JAR, UBlocks.LIGHTNING_JAR, UBlocks.STORM_JAR
        ).forEach(jar -> addDrop(jar, UBlockLootTableProvider::dropsWithSilkTouch));

        // doors
        List.of(
            UBlocks.CLOUD_DOOR, UBlocks.CRYSTAL_DOOR,
            UBlocks.DARK_OAK_DOOR, UBlocks.PALM_DOOR, UBlocks.STABLE_DOOR
        ).forEach(door -> addDrop(door, this::doorDrops));

        //beds
        List.of(
            UBlocks.CLOUD_BED, UBlocks.CLOTH_BED
        ).forEach(bed -> addDrop(bed, b -> dropsWithProperty(b, BedBlock.PART, BedPart.HEAD)));

        addDrop(UBlocks.CHITIN_SPIKES, drops(UBlocks.CHITIN_SPIKES, UItems.CARAPACE, exactly(6)));
        addDrop(UBlocks.CHITIN, drops(UBlocks.CHITIN, UItems.CARAPACE, exactly(9)));
        addDrop(UBlocks.SURFACE_CHITIN, drops(UBlocks.SURFACE_CHITIN, UItems.CARAPACE, exactly(9)));
        addDrop(UBlocks.CHISELLED_CHITIN_HULL, hullDrops(UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHITIN, UBlocks.CHISELLED_CHITIN));

        addDrop(UBlocks.SLIME_PUSTULE, LootTable.builder()
                .pool(applyStateCondition(UBlocks.SLIME_PUSTULE, SlimePustuleBlock.SHAPE, SlimePustuleBlock.Shape.POD,
                        addSurvivesExplosionCondition(UBlocks.SLIME_PUSTULE, LootPool.builder()
                    .rolls(exactly(1))
                    .with(ItemEntry.builder(UBlocks.SLIME_PUSTULE)).conditionally(WITH_SILK_TOUCH_OR_SHEARS))
        )));
        addDrop(UBlocks.MYSTERIOUS_EGG, LootTable.builder()
            .pool(addSurvivesExplosionCondition(UBlocks.MYSTERIOUS_EGG, LootPool.builder()
                .rolls(exactly(1))
                .with(ItemEntry.builder(UBlocks.MYSTERIOUS_EGG))
                    .apply(PileBlock.COUNT.getValues(), count -> applyStateCondition(UBlocks.MYSTERIOUS_EGG, PileBlock.COUNT, count, SetCountLootFunction.builder(exactly(count)))))));

        addDrop(UBlocks.CLOUD, drops(UBlocks.CLOUD, UItems.CLOUD_LUMP, exactly(4)));
        addDrop(UBlocks.CLOUD_STAIRS, drops(UBlocks.CLOUD_STAIRS, UItems.CLOUD_LUMP, exactly(6)));

        addDrop(UBlocks.SOGGY_CLOUD, drops(UBlocks.CLOUD, UItems.CLOUD_LUMP, exactly(4)));
        addDrop(UBlocks.SOGGY_CLOUD_STAIRS, drops(UBlocks.CLOUD_STAIRS, UItems.CLOUD_LUMP, exactly(6)));

        addDrop(UBlocks.DENSE_CLOUD, drops(UBlocks.DENSE_CLOUD, UItems.CLOUD_LUMP, exactly(9)));
        addDrop(UBlocks.DENSE_CLOUD_STAIRS, drops(UBlocks.DENSE_CLOUD_STAIRS, UItems.CLOUD_LUMP, exactly(13)));
        addDrop(UBlocks.ETCHED_CLOUD, drops(UBlocks.ETCHED_CLOUD, UItems.CLOUD_LUMP, exactly(9)));
        addDrop(UBlocks.ETCHED_CLOUD_STAIRS, drops(UBlocks.ETCHED_CLOUD_STAIRS, UItems.CLOUD_LUMP, exactly(13)));

        // recipe produces: 6 blocks -> 3 pillars means: 6/3 = 2
        addDrop(UBlocks.CLOUD_PILLAR, drops(UBlocks.CLOUD_PILLAR, UBlocks.CLOUD, exactly(2)));

        addDrop(UBlocks.FROSTED_OBSIDIAN, Blocks.OBSIDIAN);
        addDrop(UBlocks.APPLE_PIE, pieDrops(UBlocks.APPLE_PIE, UItems.APPLE_PIE, UItems.APPLE_PIE_HOOF));

        // crops
        addTallCropDrops(UBlocks.OATS, UItems.OATS);
        addDrop(UBlocks.BANANAS, LootTable.builder()
            .pool(addSurvivesExplosionCondition(UBlocks.BANANAS, LootPool.builder()
                .rolls(exactly(1))
                .with(item(UItems.BANANA, between(6, 12F)).apply(FORTUNE_BONUS))
            )));
        addDrop(UBlocks.PINEAPPLE, LootTable.builder()
            .pool(addSurvivesExplosionCondition(UBlocks.PINEAPPLE, LootPool.builder()
                .rolls(exactly(1))
                .with(item(UItems.PINEAPPLE, between(6, 12F))
                    .apply(FORTUNE_BONUS)
                    .conditionally(BlockStatePropertyLootCondition.builder(UBlocks.PINEAPPLE).properties(StatePredicate.Builder.create()
                        .exactMatch(Properties.BLOCK_HALF, BlockHalf.TOP)
                        .exactMatch(Properties.AGE_7, Properties.AGE_7_MAX))))
            )));
        addDrop(UBlocks.ROCKS, applyExplosionDecay(UBlocks.ROCKS, LootTable.builder()
                .pool(applyStateCondition(UBlocks.ROCKS, Properties.AGE_7, Properties.AGE_7_MAX, LootPool.builder()
                    .rolls(exactly(1))
                    .with(ItemEntry.builder(UItems.WEIRD_ROCK).conditionally(RandomChanceLootCondition.builder(0.25F)).apply(FORTUNE_BONUS))
                    .with(ItemEntry.builder(UItems.ROCK).apply(FORTUNE_BONUS))))
                .pool(LootPool.builder()
                    .rolls(exactly(1))
                    .with(ItemEntry.builder(UItems.PEBBLES)))
        ));
        addDrop(UBlocks.GOLD_ROOT, applyExplosionDecay(UBlocks.GOLD_ROOT, LootTable.builder()
            .pool(LootPool.builder().with(ItemEntry.builder(Items.GOLDEN_CARROT)))
            .pool(applyStateCondition(UBlocks.GOLD_ROOT, CarrotsBlock.AGE, 7, LootPool.builder())
                    .with(ItemEntry.builder(Items.GOLDEN_CARROT).apply(FORTUNE_BONUS)))));
        addDrop(UBlocks.PLUNDER_VINE, applyExplosionDecay(UBlocks.PLUNDER_VINE, LootTable.builder()
                .pool(LootPool.builder().rolls(exactly(4))
                    .with(ItemEntry.builder(Items.STICK))
                    .with(ItemEntry.builder(Items.DEAD_BUSH)))
                .pool(LootPool.builder().rolls(exactly(1))
                        .with(ItemEntry.builder(Items.STICK))
                        .with(ItemEntry.builder(Items.DEAD_BUSH))
                        .with(ItemEntry.builder(UItems.GRYPHON_FEATHER)))
        ));

        // hay
        addDrop(UBlocks.HAY_BLOCK, b -> edibleBlockDrops(b, Items.WHEAT));

        // shells
        addDrop(UBlocks.CLAM_SHELL, shellDrops(UBlocks.CLAM_SHELL, UItems.CLAM_SHELL));
        addDrop(UBlocks.SCALLOP_SHELL, shellDrops(UBlocks.SCALLOP_SHELL, UItems.SCALLOP_SHELL));
        addDrop(UBlocks.TURRET_SHELL, shellDrops(UBlocks.TURRET_SHELL, UItems.TURRET_SHELL));

        var farmersDelightGenerator = withConditions(DefaultResourceConditions.allModsLoaded("farmersdelight"));
        farmersDelightGenerator.addDrop(Datagen.getOrCreateBaleBlock(Unicopia.id("rice_block")), b -> edibleBlockDrops(b, Datagen.getOrCreateItem(new Identifier("farmersdelight", "rice_panicle"))));
        farmersDelightGenerator.addDrop(Datagen.getOrCreateBaleBlock(Unicopia.id("straw_block")), b -> edibleBlockDrops(b, Datagen.getOrCreateItem(new Identifier("farmersdelight", "straw"))));
    }

    private void addTallCropDrops(SegmentedCropBlock baseCrop, ItemConvertible crop) {
        addDrop(baseCrop, applyExplosionDecay(baseCrop, LootTable.builder()
            .pool(LootPool.builder()
                .rolls(exactly(1))
                .with(ItemEntry.builder(baseCrop.getSeedsItem()))))
            .pool(applyStateCondition(baseCrop, baseCrop.getAgeProperty(), baseCrop.getMaxAge(), LootPool.builder()
                .rolls(exactly(1))
                .with(ItemEntry.builder(baseCrop.getSeedsItem()).apply(FORTUNE_BONUS)))));

        SegmentedCropBlock stage = baseCrop;
        while ((stage = stage.getNext()) != null) {
            addDrop(stage, applyExplosionDecay(stage, LootTable.builder()
                .pool(LootPool.builder()
                    .rolls(exactly(1))
                    .with(applyStateCondition(stage, stage.getAgeProperty(), stage.getMaxAge(), ItemEntry.builder(crop))))));
        }
    }

    private LootTable.Builder decomposingSlabDrops(Block slab, ItemConvertible drop, int count) {
        return LootTable.builder()
            .pool(applyExplosionDecay(slab, LootPool.builder()
                .rolls(exactly(1))
                .with(item(drop, exactly(count))
                        .apply(applyStateCondition(slab, SlabBlock.TYPE, SlabType.DOUBLE, SetCountLootFunction.builder(exactly(count * 2)))))));
    }

    private LootTable.Builder fruitLeavesDrops(Block leaves) {
        return LootTable.builder()
            .pool(LootPool.builder()
                .rolls(exactly(1))
                .with(ItemEntry.builder(leaves).conditionally(WITH_SILK_TOUCH_OR_SHEARS)))
            .pool(applyExplosionDecay(leaves, LootPool.builder()
                .rolls(exactly(1))
                .conditionally(WITHOUT_SILK_TOUCH_NOR_SHEARS)
                .with(item(Items.STICK, between(1, 2)).conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, LEAVES_STICK_DROP_CHANCE)))));
    }

    private LootTable.Builder hullDrops(Block hull, ItemConvertible inner, ItemConvertible outer) {
        return LootTable.builder()
            .pool(addSurvivesExplosionCondition(hull, LootPool.builder()
                .rolls(exactly(1))
                .with(item(hull, exactly(2)).conditionally(WITHOUT_SILK_TOUCH))
                .with(item(inner, exactly(2)).conditionally(WITHOUT_SILK_TOUCH))
                .with(item(outer, exactly(2)).conditionally(WITH_SILK_TOUCH))));
    }

    private LootTable.Builder edibleBlockDrops(Block block, ItemConvertible drop) {
        LootTable.Builder builder = LootTable.builder();
        for (BooleanProperty segment : EdibleBlock.SEGMENTS) {
            builder
                .pool(addSurvivesExplosionCondition(block, LootPool.builder()
                    .rolls(exactly(1))
                        .with(applyStateCondition(block, segment, true, ItemEntry.builder(drop)))));
        }
        return builder;
    }

    private LootTable.Builder pieDrops(Block block, Item drop, Item stomped) {
        return LootTable.builder()
            .pool(applyExplosionDecay(block, LootPool.builder()
                .rolls(exactly(1)).conditionally(WITH_SILK_TOUCH)
                .with(applyStateCondition(block, PieBlock.STOMPED, false, ItemEntry.builder(drop)))
                .with(applyStateCondition(block, PieBlock.STOMPED, true, ItemEntry.builder(stomped)))));
    }

    private LootTable.Builder shellDrops(Block block, Item shell) {
        return LootTable.builder()
            .pool(applyExplosionDecay(block, LootPool.builder()
                .rolls(exactly(1))
                .with(ItemEntry.builder(shell))
                    .apply(ShellsBlock.COUNT.getValues(), count -> applyStateCondition(block, ShellsBlock.COUNT, count, SetCountLootFunction.builder(exactly(count))))
                    .apply(FORTUNE_BONUS)));
    }


    public LootTable.Builder fortuneBonusDrops(ItemConvertible drop) {
        return LootTable.builder().pool(addSurvivesExplosionCondition(drop, LootPool.builder()
                .rolls(exactly(1))
                .with(ItemEntry.builder(drop).apply(FORTUNE_BONUS))));
    }

    public static ConstantLootNumberProvider exactly(float n) {
        return ConstantLootNumberProvider.create(n);
    }

    public static UniformLootNumberProvider between(float from, float to) {
        return UniformLootNumberProvider.create(from, to);
    }

    public static ItemEntry.Builder<?> item(ItemConvertible item, LootNumberProvider count) {
        return ItemEntry.builder(item).apply(SetCountLootFunction.builder(count));
    }

    public static <T extends LootConditionConsumingBuilder<T>, P extends Comparable<P> & StringIdentifiable> T applyStateCondition(Block block,
            Property<P> property, P value, LootConditionConsumingBuilder<T> builder) {
        return builder.conditionally(BlockStatePropertyLootCondition.builder(block).properties(StatePredicate.Builder.create().exactMatch(property, value)));
    }

    public static <T extends LootConditionConsumingBuilder<T>> T applyStateCondition(Block block,
            BooleanProperty property, boolean value, LootConditionConsumingBuilder<T> builder) {
        return builder.conditionally(BlockStatePropertyLootCondition.builder(block).properties(StatePredicate.Builder.create().exactMatch(property, value)));
    }

    public static <T extends LootConditionConsumingBuilder<T>> T applyStateCondition(Block block,
            IntProperty property, int value, LootConditionConsumingBuilder<T> builder) {
        return builder.conditionally(BlockStatePropertyLootCondition.builder(block).properties(StatePredicate.Builder.create().exactMatch(property, value)));
    }
}
