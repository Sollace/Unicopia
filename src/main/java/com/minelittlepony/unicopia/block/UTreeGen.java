package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.block.*;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.Registry;
import net.minecraft.tag.BlockTags;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.JungleFoliagePlacer;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import net.minecraft.world.gen.trunk.UpwardsBranchingTrunkPlacer;

public interface UTreeGen {
    Tree ZAP_APPLE_TREE = Tree.Builder.create(Unicopia.id("zap_apple_tree"), new UpwardsBranchingTrunkPlacer(
                    7, 2, 3,
                    UniformIntProvider.create(3, 6),
                    0.3f,
                    UniformIntProvider.create(1, 3),
                    Registry.BLOCK.getOrCreateEntryList(BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH)
            ), new JungleFoliagePlacer(
                    ConstantIntProvider.create(3),
                    ConstantIntProvider.create(2),
                    3
            )
        )
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

    static Tree createAppleTree(String name, Block leaves, int preferredDensity) {
        return Tree.Builder.create(Unicopia.id(name + "_tree"),
                new StraightTrunkPlacer(4, 6, 2),
                new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3)
            )
                .farmingCondition(1, preferredDensity - 2, preferredDensity)
                .log(Blocks.OAK_LOG)
                .leaves(leaves)
                .sapling(Unicopia.id(name + "_sapling"))
                .build();
    }

    static void bootstrap() { }
}
