package com.minelittlepony.unicopia.server.world;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.block.UBlocks;

import net.fabricmc.fabric.api.biome.v1.*;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.TrunkPlacer;

public record Tree (
        Identifier id,
        TreeFeatureConfig.Builder config,
        RegistryKey<ConfiguredFeature<?, ?>> configuredFeatureId,
        Set<Placement> placements,
        Optional<Block> sapling,
        Optional<Block> pot
    ) {
    public static final List<Tree> REGISTRY = new ArrayList<>();

    private static void bootstrap() {
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.getOptional(RegistryKeys.CONFIGURED_FEATURE).ifPresent(registry -> {
                REGISTRY.forEach(tree -> {
                    Registry.register(registry, tree.id(), new ConfiguredFeature<>(Feature.TREE, tree.config.build()));
                });
            });
            registries.getOptional(RegistryKeys.PLACED_FEATURE).ifPresent(registry -> {
                var reg = registries.asDynamicRegistryManager().createRegistryLookup().getOrThrow(RegistryKeys.CONFIGURED_FEATURE);
                REGISTRY.stream().forEach(tree -> {
                    tree.placements().forEach(placement -> {
                        Registry.register(registry, placement.id(), new PlacedFeature(reg.getOrThrow(tree.configuredFeatureId()),
                                VegetationPlacedFeatures.treeModifiersWithWouldSurvive(placement.count(), tree.sapling().orElse(Blocks.OAK_SAPLING))
                        ));
                    });
                });
            });
        });
    }

    public static class Builder {
        public static final Predicate<BiomeSelectionContext> IS_FOREST = BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(BiomeTags.IS_FOREST));
        public static final Predicate<BiomeSelectionContext> IS_OAK_FOREST = IS_FOREST
                .and(BiomeSelectors.excludeByKey(BiomeKeys.BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeKeys.DARK_FOREST))
                .and(BiomeSelectors.tag(BiomeTags.IS_TAIGA).negate());

        public static Builder create(Identifier id, TrunkPlacer trunkPlacer, FoliagePlacer foliagePlacer) {
            return new Builder(id, trunkPlacer, foliagePlacer);
        }

        private Block logType = Blocks.OAK_LOG;
        private Block leavesType = Blocks.OAK_LEAVES;
        private Optional<Identifier> saplingId = Optional.empty();
        private BiFunction<SaplingGenerator, Block.Settings, SaplingBlock> saplingConstructor = SaplingBlock::new;

        private final TrunkPlacer trunkPlacer;
        private final FoliagePlacer foliagePlacer;

        private final Identifier id;

        private Map<Identifier, Placement> placements = new HashMap<>();
        private Function<TreeFeatureConfig.Builder, TreeFeatureConfig.Builder> configParameters = Function.identity();
        private Optional<TwoLayersFeatureSize> size = Optional.empty();

        private Builder(Identifier id, TrunkPlacer trunkPlacer, FoliagePlacer foliagePlacer) {
            this.id = id;
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

        public Builder sapling(Identifier saplingId) {
            this.saplingId = Optional.of(saplingId);
            return this;
        }

        public Builder sapling(BiFunction<SaplingGenerator, Block.Settings, SaplingBlock> constructor) {
            saplingConstructor = constructor;
            return this;
        }

        public Builder placement(int count, float extraChance, int extraCount, Predicate<BiomeSelectionContext> selector) {
            return placement("", count, extraChance, extraCount, selector);
        }

        public Builder placement(String suffex, int count, float extraChance, int extraCount, Predicate<BiomeSelectionContext> selector) {
            Identifier id = this.id.withSuffixedPath("/placed" + (suffex.isEmpty() ? "" : "/") + suffex);
            placements.put(id, new Placement(
                    id,
                    PlacedFeatures.createCountExtraModifier(count, extraChance, extraCount),
                    RegistryKey.of(RegistryKeys.PLACED_FEATURE, id),
                    selector
            ));
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

        public Tree build() {
            RegistryKey<ConfiguredFeature<?, ?>> configuredFeatureId = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id);
            Optional<Block> sapling = saplingId.map(id -> UBlocks.register(id, saplingConstructor.apply(new SaplingGenerator() {
                @Override
                protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random rng, boolean flowersNearby) {
                    return configuredFeatureId;
                }
            }, FabricBlockSettings.copy(Blocks.OAK_SAPLING)), ItemGroups.NATURAL));
            Tree tree = new Tree(id, configParameters.apply(new TreeFeatureConfig.Builder(
                    BlockStateProvider.of(logType),
                    trunkPlacer,
                    BlockStateProvider.of(leavesType),
                    foliagePlacer,
                    size.get()
                )), configuredFeatureId, placements.values().stream()
                    .collect(Collectors.toUnmodifiableSet()),
                    sapling,
                    sapling.map(saplingBlock -> {
                        Block flowerPot = Registry.register(Registries.BLOCK, saplingId.get().withPrefixedPath("potted_"), Blocks.createFlowerPotBlock(saplingBlock));
                        UBlocks.TRANSLUCENT_BLOCKS.add(flowerPot);
                        return flowerPot;
                    }));

            if (REGISTRY.isEmpty()) {
                bootstrap();
            }

            REGISTRY.add(tree);
            tree.placements().forEach(placement -> {
                BiomeModifications.addFeature(placement.selector(), GenerationStep.Feature.VEGETAL_DECORATION, placement.feature());
            });
            return tree;
        }
    }

    public record Placement(Identifier id, PlacementModifier count, RegistryKey<PlacedFeature> feature, Predicate<BiomeSelectionContext> selector) {

    }
}
