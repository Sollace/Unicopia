package com.minelittlepony.unicopia.server.world;

import com.google.common.collect.ImmutableList;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.gen.FruitBlobFoliagePlacer;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.JungleFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.LeavesVineTreeDecorator;
import net.minecraft.world.gen.treedecorator.TrunkVineTreeDecorator;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import net.minecraft.world.gen.trunk.UpwardsBranchingTrunkPlacer;

public interface UTreeGen {
    Tree ZAP_APPLE_TREE = Tree.Builder.create(Unicopia.id("zap_apple_tree"), new UpwardsBranchingTrunkPlacer(
                    5, 3, 0,
                    UniformIntProvider.create(3, 6),
                    0.3f,
                    UniformIntProvider.create(1, 3),
                    Registries.BLOCK.getOrCreateEntryList(BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH)
            ), new JungleFoliagePlacer(
                    ConstantIntProvider.create(3),
                    ConstantIntProvider.create(2),
                    3
            )
        )
            .configure(TreeFeatureConfig.Builder::forceDirt)
            .log(UBlocks.ZAP_LOG)
            .leaves(UBlocks.ZAP_LEAVES)
            .sapling(Unicopia.id("zapling"))
            .placement(0, 0.01F, 1, Tree.Builder.IS_FOREST)
            .dimensions(6, 0, 8)
            .build();
    Tree GREEN_APPLE_TREE = createAppleTree("green_apple", UBlocks.GREEN_APPLE_LEAVES, 2, 0.2F).build();
    Tree SWEET_APPLE_TREE = createAppleTree("sweet_apple", UBlocks.SWEET_APPLE_LEAVES, 3, 0.1F)
            .placement("orchard", 6, 0.1F, 3, BiomeSelectors.includeByKey(UWorldGen.SWEET_APPLE_ORCHARD))
            .build();
    Tree SOUR_APPLE_TREE = createAppleTree("sour_apple", UBlocks.SOUR_APPLE_LEAVES, 3, 0.2F).build();
    Tree GOLDEN_APPLE_TREE = Tree.Builder.create(Unicopia.id("golden_oak_tree"),
            new StraightTrunkPlacer(6, 1, 3),
            new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3)
        )
            .configure(TreeFeatureConfig.Builder::forceDirt)
            .dimensions(1, 3, 5)
            .log(UBlocks.GOLDEN_OAK_LOG)
            .leaves(UBlocks.GOLDEN_OAK_LEAVES)
            .sapling(Unicopia.id("golden_oak_sapling"))
            .build();
    Tree BANANA_TREE = Tree.Builder.create(Unicopia.id("banana_tree"),
            new StraightTrunkPlacer(4, 5, 3),
            new FernFoliagePlacer(ConstantIntProvider.create(4), ConstantIntProvider.create(0))
        )
            .dimensions(6, 0, 8)
            .log(UBlocks.PALM_LOG)
            .leaves(UBlocks.PALM_LEAVES)
            .sapling(Unicopia.id("palm_sapling")).sapling((generator, settings) -> {
                return new SaplingBlock(generator, settings) {
                    @Override
                    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
                        return floor.isIn(BlockTags.SAND);
                    }
                };
            })
            .configure(builder -> builder.dirtProvider(BlockStateProvider.of(Blocks.SAND)))
            .placement(2, 0.01F, 1, selector -> selector.hasTag(BiomeTags.IS_BEACH) || selector.hasTag(BiomeTags.IS_JUNGLE))
            .build();
    Tree MANGO_TREE = Tree.Builder.create(Unicopia.id("mango_tree"),
            new StraightTrunkPlacer(4, 7, 3),
            new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3)
        )
            .dimensions(9, 0, 4)
            .log(Blocks.JUNGLE_LOG)
            .leaves(UBlocks.MANGO_LEAVES)
            .sapling(Unicopia.id("mango_sapling"))
            .placement(1, 1, 2, selector -> selector.hasTag(BiomeTags.IS_JUNGLE) && selector.getBiomeKey() != BiomeKeys.SPARSE_JUNGLE)
            .configure(builder -> builder.decorators(ImmutableList.of(TrunkVineTreeDecorator.INSTANCE, new LeavesVineTreeDecorator(0.25f))))
            .build();

    static Tree.Builder createAppleTree(String name, Block leaves, int preferredDensity, float spawnRate) {
        return Tree.Builder.create(Unicopia.id(name + "_tree"),
                new StraightTrunkPlacer(4, 3, 2),
                new FruitBlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3)
            )
                .configure(TreeFeatureConfig.Builder::forceDirt)
                .placement(0, spawnRate, 4, Tree.Builder.IS_OAK_FOREST)
                .dimensions(1, preferredDensity - 2, preferredDensity)
                .log(Blocks.OAK_LOG)
                .leaves(leaves)
                .sapling(Unicopia.id(name + "_sapling"));
    }

    static void bootstrap() {
    }
}
