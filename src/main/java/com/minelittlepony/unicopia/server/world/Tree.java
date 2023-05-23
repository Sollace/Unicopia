package com.minelittlepony.unicopia.server.world;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.TrunkPlacer;

public record Tree (
        Identifier id,
        TreeFeatureConfig.Builder config,
        RegistryKey<ConfiguredFeature<?, ?>> configuredFeatureId,
        Optional<RegistryKey<PlacedFeature>> placedFeatureId,
        Optional<Block> sapling,
        Optional<PlacementModifier> placement
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
                REGISTRY.stream().filter(tree -> tree.placedFeatureId().isPresent()).forEach(tree -> {
                    var placedFeature = new PlacedFeature(reg.getOrThrow(tree.configuredFeatureId()),
                            VegetationPlacedFeatures.modifiersWithWouldSurvive(tree.placement().orElseThrow(), tree.sapling().orElse(Blocks.OAK_SAPLING))
                    );

                    Registry.register(registry, tree.id, placedFeature);
                });
            });
        });

    }

    public static class Builder {
        public static final Predicate<BiomeSelectionContext> IS_FOREST = BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(BiomeTags.IS_FOREST));

        public static Builder create(Identifier id, TrunkPlacer trunkPlacer, FoliagePlacer foliagePlacer) {
            return new Builder(id, trunkPlacer, foliagePlacer);
        }

        private Block logType = Blocks.OAK_LOG;
        private Block leavesType = Blocks.OAK_LEAVES;
        private Optional<Identifier> saplingId = Optional.empty();

        private final TrunkPlacer trunkPlacer;
        private final FoliagePlacer foliagePlacer;

        private final Identifier id;

        private Optional<Predicate<BiomeSelectionContext>> selector = Optional.empty();
        private Optional<PlacementModifier> countModifier = Optional.empty();
        private Optional<Supplier<TreeFeatureConfig.Builder>> configSupplier = Optional.empty();
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

        public Builder count(int count, float extraChance, int extraCount) {
            countModifier = Optional.of(PlacedFeatures.createCountExtraModifier(count, extraChance, extraCount));
            return this;
        }

        public Builder biomes(Predicate<BiomeSelectionContext> selector) {
            this.selector = Optional.of(selector);
            return this;
        }

        public Builder shape(Supplier<TreeFeatureConfig.Builder> shape) {
            this.configSupplier = Optional.of(shape);
            return this;
        }

        public Builder farmingCondition(int yLevel, int sizeBelowY, int sizeAboveY) {
            this.size = Optional.of(new TwoLayersFeatureSize(yLevel, Math.max(0, sizeBelowY), Math.max(0, sizeAboveY)));
            return this;
        }

        public Tree build() {
            RegistryKey<ConfiguredFeature<?, ?>> configuredFeatureId = RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, id);
            Tree tree = new Tree(id, configSupplier.map(Supplier::get)
                    .orElseGet(() -> new TreeFeatureConfig.Builder(
                            BlockStateProvider.of(logType),
                            trunkPlacer,
                            BlockStateProvider.of(leavesType),
                            foliagePlacer,
                            size.get()
                        ).forceDirt()), configuredFeatureId, selector.map(selector -> {
                RegistryKey<PlacedFeature> i = RegistryKey.of(RegistryKeys.PLACED_FEATURE, id);
                BiomeModifications.addFeature(selector, GenerationStep.Feature.VEGETAL_DECORATION, i);
                return i;
            }), saplingId.map(id -> UBlocks.register(id, new SaplingBlock(new SaplingGenerator() {
                @Override
                protected RegistryKey<ConfiguredFeature<?, ?>> getTreeFeature(Random rng, boolean flowersNearby) {
                    return configuredFeatureId;
                }
            }, FabricBlockSettings.copy(Blocks.OAK_SAPLING)), ItemGroups.NATURAL)), countModifier);

            if (REGISTRY.isEmpty()) {
                bootstrap();
            }

            REGISTRY.add(tree);
            return tree;
        }
    }
}
