package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.Tree;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class UBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public UBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected TagBuilder getTagBuilder(TagKey<Block> tag) {
        return super.getTagBuilder(tag);
    }

    @Override
    protected void configure(WrapperLookup registries) {
        populateConventionalTags();
        Block[] crops = {
                UBlocks.OATS, UBlocks.OATS_STEM, UBlocks.OATS_CROWN,
                UBlocks.ROCKS, UBlocks.PINEAPPLE,
                UBlocks.SWEET_APPLE_SPROUT, UBlocks.GREEN_APPLE_SPROUT, UBlocks.SWEET_APPLE_SPROUT,
                UBlocks.GOLDEN_OAK_SPROUT
        };

        getOrCreateTagBuilder(UTags.Blocks.CATAPULT_IMMUNE).add(
                Blocks.STRUCTURE_VOID, Blocks.STRUCTURE_BLOCK,
                Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK,
                Blocks.LIGHT, Blocks.JIGSAW, Blocks.BARRIER, Blocks.BEDROCK
        ).forceAddTag(BlockTags.DOORS).forceAddTag(BlockTags.TRAPDOORS);
        getOrCreateTagBuilder(UTags.Blocks.BUTTERFLIES_SPAWNABLE_ON).forceAddTag(BlockTags.ANIMALS_SPAWNABLE_ON).forceAddTag(BlockTags.LEAVES).forceAddTag(BlockTags.FLOWERS).forceAddTag(BlockTags.FLOWER_POTS);
        getOrCreateTagBuilder(UTags.Blocks.JARS).add(UBlocks.JAR, UBlocks.CLOUD_JAR, UBlocks.STORM_JAR, UBlocks.LIGHTNING_JAR, UBlocks.ZAP_JAR);
        getOrCreateTagBuilder(BlockTags.CROPS).add(crops);
        getOrCreateTagBuilder(BlockTags.BEE_GROWABLES).add(crops);
        getOrCreateTagBuilder(BlockTags.MAINTAINS_FARMLAND).add(crops);
        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL).add(UBlocks.FROSTED_OBSIDIAN);
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(UBlocks.ROCKS, UBlocks.FROSTED_OBSIDIAN, UBlocks.WEATHER_VANE).forceAddTag(UTags.Blocks.JARS);
        getOrCreateTagBuilder(BlockTags.DRAGON_IMMUNE).add(UBlocks.FROSTED_OBSIDIAN, UBlocks.GOLDEN_OAK_LOG, UBlocks.GOLDEN_OAK_LEAVES);
        getOrCreateTagBuilder(BlockTags.FIRE).add(UBlocks.SPECTRAL_FIRE);
        getOrCreateTagBuilder(BlockTags.HOE_MINEABLE).add(UBlocks.HAY_BLOCK).addOptional(Unicopia.id("rice_block")).addOptional(Unicopia.id("straw_block"));
        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE).add(UBlocks.WORM_BLOCK);
        getOrCreateTagBuilder(BlockTags.REPLACEABLE_BY_TREES).add(UBlocks.GREEN_APPLE, UBlocks.SOUR_APPLE, UBlocks.GOLDEN_APPLE, UBlocks.SWEET_APPLE, UBlocks.ZAP_APPLE, UBlocks.ZAP_BULB);

        addZapWoodset();
        addPalmWoodset();
        addCloudBlocksets();
        addChitinBlocksets();
        addFruitTrees();

        getOrCreateTagBuilder(UTags.Blocks.CRYSTAL_HEART_BASE).add(
                Blocks.DIAMOND_BLOCK,
                Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_SLAB, Blocks.QUARTZ_STAIRS, Blocks.QUARTZ_PILLAR,
                Blocks.SMOOTH_QUARTZ, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ_STAIRS, Blocks.CHISELED_QUARTZ_BLOCK,
                Blocks.AMETHYST_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.EMERALD_BLOCK
        );
        getOrCreateTagBuilder(UTags.Blocks.CRYSTAL_HEART_ORNAMENT).add(Blocks.END_ROD);

        getOrCreateTagBuilder(UTags.Blocks.FRAGILE)
            .forceAddTag(ConventionalBlockTags.GLASS_BLOCKS)
            .forceAddTag(ConventionalBlockTags.GLASS_PANES)
            .forceAddTag(UTags.Blocks.JARS)
            .add(Blocks.VINE, Blocks.LILY_PAD);

        getOrCreateTagBuilder(UTags.Blocks.MIMIC_CHESTS).add(
                Blocks.CHEST, Blocks.TRAPPED_CHEST, UBlocks.CLOUD_CHEST
        );

        getOrCreateTagBuilder(UTags.Blocks.INTERESTING).add(
                Blocks.SEA_LANTERN, Blocks.ENDER_CHEST, Blocks.END_PORTAL_FRAME,
                Blocks.JUKEBOX, Blocks.SPAWNER
        ).forceAddTag(ConventionalBlockTags.ORES);

        getOrCreateTagBuilder(UTags.Blocks.KICKS_UP_DUST).forceAddTag(BlockTags.SAND).add(
                Blocks.SUSPICIOUS_SAND,
                Blocks.GRAVEL, Blocks.SUSPICIOUS_GRAVEL
        ).forceAddTag(TagKey.of(RegistryKeys.BLOCK, new Identifier("c", "concrete_powders")));

        getOrCreateTagBuilder(UTags.Blocks.UNAFFECTED_BY_GROW_ABILITY).add(Blocks.GRASS_BLOCK);
    }

    private void addFruitTrees() {
        Block[] leaves = {
                UBlocks.GREEN_APPLE_LEAVES, UBlocks.SWEET_APPLE_LEAVES, UBlocks.SOUR_APPLE_LEAVES,
                UBlocks.GOLDEN_OAK_LEAVES, UBlocks.MANGO_LEAVES
        };

        getOrCreateTagBuilder(BlockTags.LEAVES).add(leaves);
        getOrCreateTagBuilder(BlockTags.HOE_MINEABLE).add(leaves);

        Block[] burnableLogs = { UBlocks.GOLDEN_OAK_LOG };
        getOrCreateTagBuilder(BlockTags.LOGS).add(burnableLogs);
        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN).add(burnableLogs);

        var saplings = Tree.REGISTRY.stream().flatMap(tree -> tree.sapling().stream()).toArray(Block[]::new);

        getOrCreateTagBuilder(BlockTags.SAPLINGS).add(saplings);
        getOrCreateTagBuilder(BlockTags.MAINTAINS_FARMLAND).add(saplings);
        getOrCreateTagBuilder(BlockTags.GUARDED_BY_PIGLINS).add(UBlocks.GOLDEN_OAK_LEAVES, UBlocks.GOLDEN_OAK_LOG, UBlocks.GOLDEN_OAK_SPROUT, UBlocks.GOLDEN_APPLE);
    }

    private void addZapWoodset() {
        getOrCreateTagBuilder(BlockTags.LEAVES).add(UBlocks.ZAP_LEAVES, UBlocks.FLOWERING_ZAP_LEAVES);
        getOrCreateTagBuilder(UTags.Blocks.POLEARM_MINEABLE).add(
                UBlocks.ZAP_LEAVES, UBlocks.FLOWERING_ZAP_LEAVES,
                UBlocks.ZAP_PLANKS,
                UBlocks.ZAP_LOG, UBlocks.ZAP_WOOD, UBlocks.STRIPPED_ZAP_LOG, UBlocks.STRIPPED_ZAP_WOOD,
                UBlocks.ZAP_FENCE_GATE, UBlocks.ZAP_FENCE,
                UBlocks.ZAP_SLAB,
                UBlocks.ZAP_STAIRS
        );

        getOrCreateTagBuilder(UTags.Blocks.ZAP_LOGS).add(UBlocks.ZAP_LOG, UBlocks.ZAP_WOOD, UBlocks.STRIPPED_ZAP_LOG, UBlocks.STRIPPED_ZAP_WOOD);
        getOrCreateTagBuilder(UTags.Blocks.WAXED_ZAP_LOGS).add(UBlocks.WAXED_ZAP_LOG, UBlocks.WAXED_ZAP_WOOD, UBlocks.WAXED_STRIPPED_ZAP_LOG, UBlocks.WAXED_STRIPPED_ZAP_WOOD);
        getOrCreateTagBuilder(BlockTags.LOGS).forceAddTag(UTags.Blocks.ZAP_LOGS).forceAddTag(UTags.Blocks.WAXED_ZAP_LOGS);
        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN).forceAddTag(UTags.Blocks.ZAP_LOGS);
        getOrCreateTagBuilder(BlockTags.PLANKS).add(UBlocks.ZAP_PLANKS, UBlocks.WAXED_ZAP_PLANKS);

        //getOrCreateTagBuilder(BlockTags.WOODEN_BUTTONS).add(UBlocks.ZAP_BUTTON);
        //getOrCreateTagBuilder(BlockTags.WOODEN_DOORS).add(UBlocks.ZAP_DOOR);
        getOrCreateTagBuilder(BlockTags.FENCE_GATES).add(UBlocks.ZAP_FENCE_GATE, UBlocks.WAXED_ZAP_FENCE_GATE);
        getOrCreateTagBuilder(BlockTags.WOODEN_FENCES).add(UBlocks.ZAP_FENCE, UBlocks.WAXED_ZAP_FENCE);
        //getOrCreateTagBuilder(BlockTags.PRESSURE_PLATES).add(UBlocks.ZAP_PRESSURE_PLATE);
        //getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES).add(UBlocks.ZAP_PRESSURE_PLATE);
        getOrCreateTagBuilder(BlockTags.SLABS).add(UBlocks.ZAP_SLAB, UBlocks.WAXED_ZAP_SLAB);
        getOrCreateTagBuilder(BlockTags.WOODEN_SLABS).add(UBlocks.ZAP_SLAB, UBlocks.WAXED_ZAP_SLAB);
        getOrCreateTagBuilder(BlockTags.STAIRS).add(UBlocks.ZAP_STAIRS, UBlocks.WAXED_ZAP_STAIRS);
        getOrCreateTagBuilder(BlockTags.WOODEN_STAIRS).add(UBlocks.ZAP_STAIRS, UBlocks.WAXED_ZAP_STAIRS);
        //getOrCreateTagBuilder(BlockTags.TRAPDOORS).add(UBlocks.ZAP_TRAPDOOR);
        //getOrCreateTagBuilder(BlockTags.WOODEN_TRAPDOORS).add(UBlocks.ZAP_TRAPDOOR);
    }

    private void addPalmWoodset() {
        getOrCreateTagBuilder(BlockTags.LEAVES).add(UBlocks.PALM_LEAVES);
        getOrCreateTagBuilder(BlockTags.HOE_MINEABLE).add(UBlocks.PALM_LEAVES);
        getOrCreateTagBuilder(UTags.Blocks.PALM_LOGS).add(UBlocks.PALM_LOG, UBlocks.PALM_WOOD, UBlocks.STRIPPED_PALM_LOG, UBlocks.STRIPPED_PALM_WOOD);
        getOrCreateTagBuilder(BlockTags.LOGS).forceAddTag(UTags.Blocks.PALM_LOGS);
        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN).forceAddTag(UTags.Blocks.PALM_LOGS);
        getOrCreateTagBuilder(BlockTags.PLANKS).add(UBlocks.PALM_PLANKS);
        addSign(UBlocks.PALM_SIGN, UBlocks.PALM_WALL_SIGN, UBlocks.PALM_HANGING_SIGN, UBlocks.PALM_WALL_HANGING_SIGN);
        getOrCreateTagBuilder(BlockTags.WOODEN_BUTTONS).add(UBlocks.PALM_BUTTON);
        getOrCreateTagBuilder(BlockTags.WOODEN_DOORS).add(UBlocks.PALM_DOOR);
        getOrCreateTagBuilder(BlockTags.FENCE_GATES).add(UBlocks.PALM_FENCE_GATE);
        getOrCreateTagBuilder(BlockTags.WOODEN_FENCES).add(UBlocks.PALM_FENCE);
        getOrCreateTagBuilder(BlockTags.PRESSURE_PLATES).add(UBlocks.PALM_PRESSURE_PLATE);
        getOrCreateTagBuilder(BlockTags.WOODEN_PRESSURE_PLATES).add(UBlocks.PALM_PRESSURE_PLATE);
        getOrCreateTagBuilder(BlockTags.SLABS).add(UBlocks.PALM_SLAB);
        getOrCreateTagBuilder(BlockTags.WOODEN_SLABS).add(UBlocks.PALM_SLAB);
        getOrCreateTagBuilder(BlockTags.STAIRS).add(UBlocks.PALM_STAIRS);
        getOrCreateTagBuilder(BlockTags.WOODEN_STAIRS).add(UBlocks.PALM_STAIRS);
        getOrCreateTagBuilder(BlockTags.TRAPDOORS).add(UBlocks.PALM_TRAPDOOR);
        getOrCreateTagBuilder(BlockTags.WOODEN_TRAPDOORS).add(UBlocks.PALM_TRAPDOOR);
    }

    private void addCloudBlocksets() {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(
                UBlocks.CLOUD_BRICKS, UBlocks.CLOUD_BRICK_SLAB, UBlocks.CLOUD_BRICK_STAIRS, UBlocks.COMPACTED_CLOUD_BRICKS, UBlocks.CARVED_CLOUD
        );
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(
                UBlocks.CLOUD_PLANKS, UBlocks.CLOUD_PLANK_SLAB, UBlocks.CLOUD_PLANK_STAIRS, UBlocks.COMPACTED_CLOUD_PLANKS
        );

        getOrCreateTagBuilder(UTags.Blocks.CLOUD_BEDS).add(UBlocks.CLOUD_BED);
        getOrCreateTagBuilder(UTags.Blocks.CLOUD_SLABS).add(
                UBlocks.CLOUD_SLAB, UBlocks.SOGGY_CLOUD_SLAB, UBlocks.DENSE_CLOUD_SLAB, UBlocks.ETCHED_CLOUD_SLAB,
                UBlocks.CLOUD_PLANK_SLAB, UBlocks.CLOUD_BRICK_SLAB
        );
        getOrCreateTagBuilder(UTags.Blocks.CLOUD_STAIRS).add(
                UBlocks.CLOUD_STAIRS, UBlocks.SOGGY_CLOUD_STAIRS, UBlocks.DENSE_CLOUD_STAIRS, UBlocks.ETCHED_CLOUD_STAIRS,
                UBlocks.CLOUD_PLANK_STAIRS, UBlocks.CLOUD_BRICK_STAIRS
        );
        getOrCreateTagBuilder(UTags.Blocks.CLOUD_BLOCKS).add(
                UBlocks.CLOUD, UBlocks.CLOUD_PLANKS, UBlocks.CLOUD_BRICKS, UBlocks.DENSE_CLOUD,
                UBlocks.ETCHED_CLOUD, UBlocks.CARVED_CLOUD, UBlocks.CLOUD_PILLAR,
                UBlocks.COMPACTED_CLOUD, UBlocks.COMPACTED_CLOUD_PLANKS, UBlocks.COMPACTED_CLOUD_BRICKS,
                UBlocks.UNSTABLE_CLOUD, UBlocks.SOGGY_CLOUD, UBlocks.SHAPING_BENCH
        );
    }

    private void addChitinBlocksets() {
        getOrCreateTagBuilder(UTags.Blocks.CHITIN_BLOCKS).add(
                UBlocks.CHITIN, UBlocks.SURFACE_CHITIN,
                UBlocks.CHISELLED_CHITIN, UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHISELLED_CHITIN_SLAB, UBlocks.CHISELLED_CHITIN_STAIRS,
                UBlocks.CHITIN_SPIKES
        );


        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(
                UBlocks.CHITIN_SPIKES,
                UBlocks.CHISELLED_CHITIN, UBlocks.CHISELLED_CHITIN_HULL, UBlocks.CHISELLED_CHITIN_SLAB, UBlocks.CHISELLED_CHITIN_STAIRS
        );
        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE).add(UBlocks.CHITIN, UBlocks.SURFACE_CHITIN);
    }

    private void addSign(Block standing, Block wall, Block hanging, Block wallHanging) {
        getOrCreateTagBuilder(BlockTags.STANDING_SIGNS).add(standing);
        getOrCreateTagBuilder(BlockTags.WALL_SIGNS).add(wall);

        getOrCreateTagBuilder(BlockTags.CEILING_HANGING_SIGNS).add(hanging);
        getOrCreateTagBuilder(BlockTags.WALL_HANGING_SIGNS).add(wallHanging);
    }

    private void populateConventionalTags() {
        getOrCreateTagBuilder(UConventionalTags.Blocks.CONCRETES).add(Arrays.stream(DyeColor.values()).map(i -> Registries.BLOCK.get(new Identifier(i.getName() + "_concrete"))).toArray(Block[]::new));
        getOrCreateTagBuilder(UConventionalTags.Blocks.CONCRETE_POWDERS).add(Arrays.stream(DyeColor.values()).map(i -> Registries.BLOCK.get(new Identifier(i.getName() + "_concrete_powder"))).toArray(Block[]::new));
        getOrCreateTagBuilder(UConventionalTags.Blocks.GLAZED_TERRACOTTAS).add(Arrays.stream(DyeColor.values()).map(i -> Registries.BLOCK.get(new Identifier(i.getName() + "_glazed_terracotta"))).toArray(Block[]::new));
        getOrCreateTagBuilder(UConventionalTags.Blocks.CORAL_BLOCKS).add(Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK);
        getOrCreateTagBuilder(UConventionalTags.Blocks.CORAL_FANS).add(Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN);
        getOrCreateTagBuilder(UConventionalTags.Blocks.CORALS).add(Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL);
    }
}
