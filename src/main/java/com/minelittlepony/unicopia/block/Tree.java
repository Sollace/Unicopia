package com.minelittlepony.unicopia.block;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.biome.v1.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.entry.*;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.trunk.TrunkPlacer;

public record Tree (
        RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> configuredFeature,
        Optional<RegistryEntry<PlacedFeature>> placedFeature,
        Optional<Block> sapling
    ) {
    public static final List<Tree> REGISTRY = new ArrayList<>();

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
            RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> configuredFeature = ConfiguredFeatures.register(id.toString(), Feature.TREE, configSupplier.map(Supplier::get)
                    .orElseGet(() -> new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(logType),
                        trunkPlacer,
                        BlockStateProvider.of(leavesType),
                        foliagePlacer,
                        size.get()
                    ).forceDirt()).build());

            Optional<Block> sapling = saplingId.map(id -> UBlocks.register(id, new SaplingBlock(new SaplingGenerator() {
                @Override
                protected RegistryEntry<? extends ConfiguredFeature<?, ?>> getTreeFeature(Random rng, boolean flowersNearby) {
                    return configuredFeature;
                }
            }, FabricBlockSettings.copy(Blocks.OAK_SAPLING)), ItemGroup.DECORATIONS));

            Optional<RegistryEntry<PlacedFeature>> placedFeature = selector.map(selector -> {
                var pf = PlacedFeatures.register(id.toString() + "_checked", configuredFeature,
                    VegetationPlacedFeatures.modifiersWithWouldSurvive(countModifier.orElseThrow(), sapling.orElse(Blocks.OAK_SAPLING))
                );
                BiomeModifications.addFeature(selector, GenerationStep.Feature.VEGETAL_DECORATION, pf.getKey().get());
                return pf;
            });

            Tree tree = new Tree(configuredFeature, placedFeature, sapling);

            REGISTRY.add(tree);
            return tree;
        }
    }
}
