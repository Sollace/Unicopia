package com.minelittlepony.unicopia.datagen.providers.loot;

import java.util.List;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.providers.UModelProvider;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.Tree;
import com.minelittlepony.unicopia.server.world.UTreeGen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;

public class UBlockLootTableProvider extends FabricBlockLootTableProvider {

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

        // fruit
        UModelProvider.FRUITS.forEach((block, drop) -> {
            if (block != UBlocks.GOLDEN_APPLE) {
                addDrop(block, drop);
            }
        });
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

        addDrop(UBlocks.CHITIN_SPIKES, drops(UBlocks.CHITIN_SPIKES, UItems.CARAPACE, ConstantLootNumberProvider.create(6)));
        addDrop(UBlocks.CHITIN, drops(UBlocks.CHITIN, UItems.CARAPACE, ConstantLootNumberProvider.create(9)));
        addDrop(UBlocks.SURFACE_CHITIN, drops(UBlocks.SURFACE_CHITIN, UItems.CARAPACE, ConstantLootNumberProvider.create(9)));

        addDrop(UBlocks.CLOUD, drops(UBlocks.CLOUD, UItems.CLOUD_LUMP, ConstantLootNumberProvider.create(4)));
        addDrop(UBlocks.CLOUD_STAIRS, drops(UBlocks.CLOUD_STAIRS, UItems.CLOUD_LUMP, ConstantLootNumberProvider.create(6)));

        addDrop(UBlocks.SOGGY_CLOUD, drops(UBlocks.CLOUD, UItems.CLOUD_LUMP, ConstantLootNumberProvider.create(4)));
        addDrop(UBlocks.SOGGY_CLOUD_STAIRS, drops(UBlocks.CLOUD_STAIRS, UItems.CLOUD_LUMP, ConstantLootNumberProvider.create(6)));

        addDrop(UBlocks.DENSE_CLOUD, drops(UBlocks.DENSE_CLOUD, UItems.CLOUD_LUMP, ConstantLootNumberProvider.create(9)));
        addDrop(UBlocks.DENSE_CLOUD_STAIRS, drops(UBlocks.DENSE_CLOUD_STAIRS, UItems.CLOUD_LUMP, ConstantLootNumberProvider.create(13)));
        addDrop(UBlocks.ETCHED_CLOUD, drops(UBlocks.ETCHED_CLOUD, UItems.CLOUD_LUMP, ConstantLootNumberProvider.create(9)));
        addDrop(UBlocks.ETCHED_CLOUD_STAIRS, drops(UBlocks.ETCHED_CLOUD_STAIRS, UItems.CLOUD_LUMP, ConstantLootNumberProvider.create(13)));

        // recipe produces: 6 blocks -> 3 pillars means: 6/3 = 2
        addDrop(UBlocks.CLOUD_PILLAR, drops(UBlocks.CLOUD_PILLAR, UBlocks.CLOUD, ConstantLootNumberProvider.create(2)));

        addDrop(UBlocks.FROSTED_OBSIDIAN, Blocks.OBSIDIAN);
    }

    private LootTable.Builder fruitLeavesDrops(Block leaves) {
        return LootTable.builder()
            .pool(LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(leaves).conditionally(WITH_SILK_TOUCH_OR_SHEARS))
            )
            .pool(LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .conditionally(WITHOUT_SILK_TOUCH_NOR_SHEARS)
                    .with(
                            applyExplosionDecay(leaves, ItemEntry.builder(Items.STICK)
                                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1, 2)))
                            )
                            .conditionally(TableBonusLootCondition.builder(Enchantments.FORTUNE, LEAVES_STICK_DROP_CHANCE))
                    )
            );
    }

}
