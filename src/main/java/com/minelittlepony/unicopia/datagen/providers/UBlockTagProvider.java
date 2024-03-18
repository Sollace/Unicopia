package com.minelittlepony.unicopia.datagen.providers;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.Tree;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;

public class UBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public UBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(WrapperLookup registries) {
        Block[] crops = {
                UBlocks.OATS, UBlocks.OATS_STEM, UBlocks.OATS_CROWN,
                UBlocks.ROCKS, UBlocks.PINEAPPLE,
                UBlocks.SWEET_APPLE_SPROUT, UBlocks.GREEN_APPLE_SPROUT, UBlocks.SWEET_APPLE_SPROUT,
                UBlocks.GOLDEN_OAK_SPROUT
        };

        getOrCreateTagBuilder(UTags.CATAPULT_IMMUNE).add(Blocks.BEDROCK).forceAddTag(BlockTags.DOORS).forceAddTag(BlockTags.TRAPDOORS);
        getOrCreateTagBuilder(BlockTags.CROPS).add(crops);
        getOrCreateTagBuilder(BlockTags.BEE_GROWABLES).add(crops);
        getOrCreateTagBuilder(BlockTags.MAINTAINS_FARMLAND).add(crops);
        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL).add(UBlocks.FROSTED_OBSIDIAN);
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(UBlocks.ROCKS, UBlocks.FROSTED_OBSIDIAN, UBlocks.WEATHER_VANE);
        getOrCreateTagBuilder(BlockTags.DRAGON_IMMUNE).add(UBlocks.FROSTED_OBSIDIAN, UBlocks.GOLDEN_OAK_LOG, UBlocks.GOLDEN_OAK_LEAVES);
        getOrCreateTagBuilder(BlockTags.FIRE).add(UBlocks.SPECTRAL_FIRE);
        getOrCreateTagBuilder(BlockTags.HOE_MINEABLE).add(UBlocks.HAY_BLOCK).addOptional(Unicopia.id("rice_block")).addOptional(Unicopia.id("straw_block"));

        addZapWoodset();
        addPalmWoodset();
        addCloudBlocksets();
        addChitinBlocksets();
        addHayBlocks();
        addFruitTrees();
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
        getOrCreateTagBuilder(UTags.POLEARM_MINEABLE).add(
                UBlocks.ZAP_LEAVES, UBlocks.FLOWERING_ZAP_LEAVES,
                UBlocks.ZAP_PLANKS,
                UBlocks.ZAP_LOG, UBlocks.ZAP_WOOD, UBlocks.STRIPPED_ZAP_LOG, UBlocks.STRIPPED_ZAP_WOOD,
                UBlocks.ZAP_FENCE_GATE, UBlocks.ZAP_FENCE,
                UBlocks.ZAP_SLAB,
                UBlocks.ZAP_STAIRS
        );

        Block[] burnableLogs = { UBlocks.WAXED_ZAP_LOG, UBlocks.WAXED_ZAP_WOOD, UBlocks.WAXED_STRIPPED_ZAP_LOG, UBlocks.WAXED_STRIPPED_ZAP_WOOD };
        getOrCreateTagBuilder(BlockTags.LOGS).add(burnableLogs).add(UBlocks.ZAP_LOG, UBlocks.ZAP_WOOD, UBlocks.STRIPPED_ZAP_LOG, UBlocks.STRIPPED_ZAP_WOOD);
        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN).add(burnableLogs);
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

        Block[] logs = { UBlocks.PALM_LOG, UBlocks.PALM_WOOD, UBlocks.STRIPPED_PALM_LOG, UBlocks.STRIPPED_PALM_WOOD };
        getOrCreateTagBuilder(BlockTags.LOGS).add(logs);
        getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN).add(logs);
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

        getOrCreateTagBuilder(UTags.block("cloud_beds")).add(UBlocks.CLOUD_BED);
        getOrCreateTagBuilder(UTags.block("cloud_slabs")).add(
                UBlocks.CLOUD_SLAB, UBlocks.SOGGY_CLOUD_SLAB, UBlocks.DENSE_CLOUD_SLAB, UBlocks.ETCHED_CLOUD_SLAB,
                UBlocks.CLOUD_PLANK_SLAB, UBlocks.CLOUD_BRICK_SLAB
        );
        getOrCreateTagBuilder(UTags.block("cloud_stairs")).add(
                UBlocks.CLOUD_STAIRS, UBlocks.SOGGY_CLOUD_STAIRS, UBlocks.DENSE_CLOUD_STAIRS, UBlocks.ETCHED_CLOUD_STAIRS,
                UBlocks.CLOUD_PLANK_STAIRS, UBlocks.CLOUD_BRICK_STAIRS
        );
        getOrCreateTagBuilder(UTags.block("clouds")).add(
                UBlocks.CLOUD, UBlocks.CLOUD_PLANKS, UBlocks.CLOUD_BRICKS, UBlocks.DENSE_CLOUD,
                UBlocks.ETCHED_CLOUD, UBlocks.CARVED_CLOUD,
                UBlocks.COMPACTED_CLOUD, UBlocks.COMPACTED_CLOUD_PLANKS, UBlocks.COMPACTED_CLOUD_BRICKS,
                UBlocks.UNSTABLE_CLOUD, UBlocks.SOGGY_CLOUD
        );
    }

    private void addChitinBlocksets() {
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

    private void addHayBlocks() {

    }
}
