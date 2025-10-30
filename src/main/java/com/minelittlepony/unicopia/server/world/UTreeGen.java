package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.server.world.gen.UBiomes;
import com.minelittlepony.unicopia.server.world.gen.UFeatureConfigs;
import com.minelittlepony.unicopia.server.world.gen.UPlacedFeatures;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;

public interface UTreeGen {
    Tree ZAP_APPLE_TREE = Tree.Builder.create(UFeatureConfigs.ZAP_APPLE_TREE)
            .sapling(Unicopia.id("zapling"))
            .spawns(UPlacedFeatures.ZAP_APPLE_TREE, Tree.Builder.IS_FOREST)
            .build();
    Tree GREEN_APPLE_TREE = createAppleTree("green_apple", UFeatureConfigs.GREEN_APPLE_TREE, UPlacedFeatures.GREEN_APPLE_TREE).build();
    Tree SWEET_APPLE_TREE = createAppleTree("sweet_apple", UFeatureConfigs.SWEET_APPLE_TREE, UPlacedFeatures.SWEET_APPLE_TREE)
            .spawns(UPlacedFeatures.SWEET_APPLE_TREE_ORCHARD, BiomeSelectors.includeByKey(UBiomes.SWEET_APPLE_ORCHARD))
            .build();
    Tree SOUR_APPLE_TREE = createAppleTree("sour_apple", UFeatureConfigs.SOUR_APPLE_TREE, UPlacedFeatures.SOUR_APPLE_TREE).build();
    Tree GOLDEN_OAK_TREE = Tree.Builder.create(UFeatureConfigs.GOLDEN_OAK_TREE)
            .sapling(Unicopia.id("golden_oak_sapling"))
            .build();
    Tree BANANA_TREE = Tree.Builder.create(UFeatureConfigs.BANANA_TREE)
            .sapling(Unicopia.id("palm_sapling")).sapling((generator, settings) -> {
                return new SaplingBlock(generator, settings) {
                    @Override
                    public boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
                        return floor.isIn(BlockTags.SAND);
                    }
                };
            })
            .spawns(UPlacedFeatures.BANANA_TREE, selector -> selector.hasTag(BiomeTags.IS_BEACH) || selector.hasTag(BiomeTags.IS_JUNGLE))
            .build();
    Tree MANGO_TREE = Tree.Builder.create(UFeatureConfigs.MANGO_TREE)
            .sapling(Unicopia.id("mango_sapling"))
            .spawns(UPlacedFeatures.MANGO_TREE, selector -> selector.hasTag(BiomeTags.IS_JUNGLE) && selector.getBiomeKey() != BiomeKeys.SPARSE_JUNGLE)
            .build();

    static Tree.Builder createAppleTree(String name, RegistryKey<ConfiguredFeature<?, ?>> featureKey, RegistryKey<PlacedFeature> placement) {
        return Tree.Builder.create(featureKey)
                .sapling(Unicopia.id(name + "_sapling"))
                .spawns(placement, Tree.Builder.IS_OAK_FOREST);
    }

    static void bootstrap() {
    }
}
