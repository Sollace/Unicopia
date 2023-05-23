package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.BlockView;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.JungleFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import net.minecraft.world.gen.trunk.UpwardsBranchingTrunkPlacer;

public interface UTreeGen {
    Tree ZAP_APPLE_TREE = Tree.Builder.create(Unicopia.id("zap_apple_tree"), new UpwardsBranchingTrunkPlacer(
                    7, 2, 3,
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
            .biomes(Tree.Builder.IS_FOREST)
            .count(0, 0.01F, 1)
            .farmingCondition(6, 0, 8)
            .build();
    Tree GREEN_APPLE_TREE = createAppleTree("green_apple", UBlocks.GREEN_APPLE_LEAVES, 2);
    Tree SWEET_APPLE_TREE = createAppleTree("sweet_apple", UBlocks.SWEET_APPLE_LEAVES, 3);
    Tree SOUR_APPLE_TREE = createAppleTree("sour_apple", UBlocks.SOUR_APPLE_LEAVES, 5);
    Tree BANANA_TREE = Tree.Builder.create(Unicopia.id("banana_tree"),
            new StraightTrunkPlacer(4, 5, 3),
            new FernFoliagePlacer(ConstantIntProvider.create(4), ConstantIntProvider.create(0))
        )
            .farmingCondition(6, 0, 8)
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
            .biomes(selector -> selector.hasTag(BiomeTags.IS_BEACH) || selector.hasTag(BiomeTags.IS_JUNGLE))
            .count(2, 0.01F, 1)
            .build();

    static Tree createAppleTree(String name, Block leaves, int preferredDensity) {
        return Tree.Builder.create(Unicopia.id(name + "_tree"),
                new StraightTrunkPlacer(4, 6, 2),
                new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3)
            )
                .configure(TreeFeatureConfig.Builder::forceDirt)
                .farmingCondition(1, preferredDensity - 2, preferredDensity)
                .log(Blocks.OAK_LOG)
                .leaves(leaves)
                .sapling(Unicopia.id(name + "_sapling"))
                .build();
    }

    static void bootstrap() { }
}
