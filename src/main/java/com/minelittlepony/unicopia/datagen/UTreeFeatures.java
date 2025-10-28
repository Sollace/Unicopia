package com.minelittlepony.unicopia.datagen;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import com.google.common.collect.ImmutableList;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.FernFoliagePlacer;
import com.minelittlepony.unicopia.server.world.Tree;
import com.minelittlepony.unicopia.server.world.UTreeGen;
import com.minelittlepony.unicopia.server.world.gen.FruitBlobFoliagePlacer;
import com.minelittlepony.unicopia.server.world.gen.UFeatureConfigs;
import com.minelittlepony.unicopia.server.world.gen.UPlacedFeatures;

import net.minecraft.block.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.JungleFoliagePlacer;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.LeavesVineTreeDecorator;
import net.minecraft.world.gen.treedecorator.TrunkVineTreeDecorator;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import net.minecraft.world.gen.trunk.TrunkPlacer;
import net.minecraft.world.gen.trunk.UpwardsBranchingTrunkPlacer;

public class UTreeFeatures {
    static void bootstrapConfiguredFeatures(Registerable<ConfiguredFeature<?, ?>> registerable) {
        registerable.register(UFeatureConfigs.ZAP_APPLE_TREE, Generator.Builder.create(Unicopia.id("zap_apple_tree"), new UpwardsBranchingTrunkPlacer(
                    5, 3, 0,
                    UniformIntProvider.create(3, 6),
                    0.3f,
                    UniformIntProvider.create(1, 3),
                    Registries.BLOCK.getOrCreateEntryList(BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH)
            ), new JungleFoliagePlacer(
                    ConstantIntProvider.create(3),
                    ConstantIntProvider.create(2),
                    3
            ))
            .configure(TreeFeatureConfig.Builder::forceDirt)
            .log(UBlocks.ZAP_LOG)
            .leaves(UBlocks.ZAP_LEAVES)
            .dimensions(6, 0, 8)
            .build());
        registerable.register(UFeatureConfigs.GREEN_APPLE_TREE, createAppleTreeGenerator("green_apple", UBlocks.GREEN_APPLE_LEAVES, 2).build());
        registerable.register(UFeatureConfigs.SWEET_APPLE_TREE, createAppleTreeGenerator("sweet_apple", UBlocks.SWEET_APPLE_LEAVES, 3).build());
        registerable.register(UFeatureConfigs.SOUR_APPLE_TREE, createAppleTreeGenerator("sour_apple", UBlocks.SOUR_APPLE_LEAVES, 3).build());
        registerable.register(UFeatureConfigs.GOLDEN_OAK_TREE, Generator.Builder.create(Unicopia.id("golden_oak_tree"),
                new StraightTrunkPlacer(6, 1, 3),
                new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3)
            ).configure(TreeFeatureConfig.Builder::forceDirt)
            .dimensions(1, 3, 5)
            .log(UBlocks.GOLDEN_OAK_LOG)
            .leaves(UBlocks.GOLDEN_OAK_LEAVES).build());
        registerable.register(UFeatureConfigs.BANANA_TREE, Generator.Builder.create(Unicopia.id("banana_tree"),
                new StraightTrunkPlacer(4, 5, 3),
                new FernFoliagePlacer(ConstantIntProvider.create(4), ConstantIntProvider.create(0))
            ).dimensions(6, 0, 8)
            .log(UBlocks.PALM_LOG)
            .leaves(UBlocks.PALM_LEAVES)
            .configure(builder -> builder.dirtProvider(BlockStateProvider.of(Blocks.SAND)))
            .build());
        registerable.register(UFeatureConfigs.MANGO_TREE, Generator.Builder.create(Unicopia.id("mango_tree"),
                new StraightTrunkPlacer(4, 7, 3),
                new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3)
            ).dimensions(9, 0, 4)
            .log(Blocks.JUNGLE_LOG)
            .leaves(UBlocks.MANGO_LEAVES)
            .configure(builder -> builder.decorators(ImmutableList.of(TrunkVineTreeDecorator.INSTANCE, new LeavesVineTreeDecorator(0.25f)))).build());
    }

    static void bootstrapPlacedFeatures(Registerable<PlacedFeature> registerable) {
        var configuredFeatures = registerable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        registerable.register(UPlacedFeatures.GREEN_APPLE_TREE, placement(configuredFeatures, UTreeGen.GREEN_APPLE_TREE, 0, 0.2F, 4));
        registerable.register(UPlacedFeatures.SWEET_APPLE_TREE, placement(configuredFeatures, UTreeGen.SWEET_APPLE_TREE, 0, 0.1F, 4));
        registerable.register(UPlacedFeatures.SWEET_APPLE_TREE_ORCHARD, placement(configuredFeatures, UTreeGen.SWEET_APPLE_TREE, 6, 0.1F, 3));
        registerable.register(UPlacedFeatures.SOUR_APPLE_TREE, placement(configuredFeatures, UTreeGen.SOUR_APPLE_TREE, 0, 0.2F, 4));
        registerable.register(UPlacedFeatures.ZAP_APPLE_TREE, placement(configuredFeatures, UTreeGen.ZAP_APPLE_TREE, 0, 0.01F, 1));
        registerable.register(UPlacedFeatures.BANANA_TREE, placement(configuredFeatures, UTreeGen.BANANA_TREE, 2, 0.01F, 1));
        registerable.register(UPlacedFeatures.MANGO_TREE, placement(configuredFeatures, UTreeGen.MANGO_TREE, 1, 1, 2));
    }

    private static PlacedFeature placement(RegistryEntryLookup<ConfiguredFeature<?, ?>> configuredFeatures, Tree tree, int count, float extraChance, int extraCount) {
        return new PlacedFeature(configuredFeatures.getOrThrow(tree.feature()),
                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(
                        PlacedFeatures.createCountExtraModifier(count, extraChance, extraCount), tree.sapling().orElse(Blocks.OAK_SAPLING))
        );
    }

    static Generator.Builder createAppleTreeGenerator(String name, Block leaves, int preferredDensity) {
        return Generator.Builder.create(
                    Unicopia.id(name + "_tree"),
                    new StraightTrunkPlacer(4, 3, 2),
                    new FruitBlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 3)
            ).configure(TreeFeatureConfig.Builder::forceDirt)
            .dimensions(1, preferredDensity - 2, preferredDensity)
            .log(Blocks.OAK_LOG)
            .leaves(leaves);
    }

    public record Generator(
            TreeFeatureConfig.Builder config,
            RegistryKey<ConfiguredFeature<?, ?>> configuredFeatureId,
            Set<Placement> placements
    ) {
        public static class Builder {
            public static Builder create(Identifier id, TrunkPlacer trunkPlacer, FoliagePlacer foliagePlacer) {
                return new Builder(trunkPlacer, foliagePlacer);
            }

            private Block logType = Blocks.OAK_LOG;
            private Block leavesType = Blocks.OAK_LEAVES;

            private final TrunkPlacer trunkPlacer;
            private final FoliagePlacer foliagePlacer;

            private Function<TreeFeatureConfig.Builder, TreeFeatureConfig.Builder> configParameters = Function.identity();
            private Optional<TwoLayersFeatureSize> size = Optional.empty();

            private Builder(TrunkPlacer trunkPlacer, FoliagePlacer foliagePlacer) {
                this.trunkPlacer = trunkPlacer;
                this.foliagePlacer = foliagePlacer;
            }

            public Builder log(Block log) {
                this.logType = log;
                return this;
            }

            public Builder leaves(Block leaves) {
                this.leavesType = leaves;
                return this;
            }

            public Builder configure(Function<TreeFeatureConfig.Builder, TreeFeatureConfig.Builder> shape) {
                this.configParameters = shape;
                return this;
            }

            public Builder dimensions(int yLevel, int sizeBelowY, int sizeAboveY) {
                this.size = Optional.of(new TwoLayersFeatureSize(yLevel, Math.max(0, sizeBelowY), Math.max(0, sizeAboveY)));
                return this;
            }

            public ConfiguredFeature<?, ?> build() {
                return new ConfiguredFeature<>(Feature.TREE, configParameters.apply(new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(logType),
                        trunkPlacer,
                        BlockStateProvider.of(leavesType),
                        foliagePlacer,
                        size.get()
                    )).build());
            }
        }
    }

    public record Placement(PlacementModifier count, RegistryKey<PlacedFeature> key) { }
}
