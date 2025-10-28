package com.minelittlepony.unicopia.server.world;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import com.minelittlepony.unicopia.block.UBlocks;
import com.mojang.datafixers.util.Pair;

import net.fabricmc.fabric.api.biome.v1.*;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.GenerationStep;

public record Tree (
        RegistryKey<ConfiguredFeature<?, ?>> feature,
        Optional<Block> sapling,
        Optional<Block> pot
    ) {
    public static final List<Tree> REGISTRY = new ArrayList<>();

    public static class Builder {
        public static final Predicate<BiomeSelectionContext> IS_FOREST = BiomeSelectors.foundInOverworld().and(BiomeSelectors.tag(BiomeTags.IS_FOREST));
        public static final Predicate<BiomeSelectionContext> IS_OAK_FOREST = IS_FOREST
                .and(BiomeSelectors.excludeByKey(BiomeKeys.BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeKeys.DARK_FOREST))
                .and(BiomeSelectors.tag(BiomeTags.IS_TAIGA).negate());

        public static Builder create(RegistryKey<ConfiguredFeature<?, ?>> featureKey) {
            return new Builder(featureKey);
        }

        private Optional<Identifier> saplingId = Optional.empty();
        private BiFunction<SaplingGenerator, Block.Settings, SaplingBlock> saplingConstructor = SaplingBlock::new;

        private final RegistryKey<ConfiguredFeature<?, ?>> featureKey;

        private final List<Pair<RegistryKey<PlacedFeature>, Predicate<BiomeSelectionContext>>> placements = new ArrayList<>();

        private Builder(RegistryKey<ConfiguredFeature<?, ?>> featureKey) {
            this.featureKey = featureKey;
        }

        public Builder spawns(RegistryKey<PlacedFeature> placement, Predicate<BiomeSelectionContext> selector) {
            placements.add(Pair.of(placement, selector));
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

        public Tree build() {
            Optional<Block> sapling = saplingId.map(id -> UBlocks.register(id, saplingConstructor.apply(
                    new SaplingGenerator(id.toString(), Optional.empty(),
                            Optional.of(featureKey),
                            Optional.empty()
                    ), Block.Settings.copy(Blocks.OAK_SAPLING)), ItemGroups.NATURAL));
            var pot = sapling.map(saplingBlock -> {
                Block flowerPot = Registry.register(Registries.BLOCK,
                        saplingId.get().withPrefixedPath("potted_"),
                        Blocks.createFlowerPotBlock(saplingBlock)
                );
                UBlocks.TRANSLUCENT_BLOCKS.add(flowerPot);
                return flowerPot;
            });
            placements.forEach(placement -> {
                BiomeModifications.addFeature(placement.getSecond(), GenerationStep.Feature.VEGETAL_DECORATION, placement.getFirst());
            });
            Tree tree = new Tree(featureKey, sapling, pot);
            REGISTRY.add(tree);
            return tree;
        }
    }
}
