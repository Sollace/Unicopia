package com.minelittlepony.unicopia.structure;

import com.google.common.collect.ImmutableList;

import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.CountExtraChanceDecoratorConfig;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.RandomFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public interface UStructures {
    StructurePieceType CLOUD_HOUSE_PART = part("cloud_house", CloudDungeonFeature.Piece::new);
    StructurePieceType RUIN_PART = part("ruin", RuinFeature.Piece::new);

    StructureFeature<DefaultFeatureConfig> CLOUD_HOUSE = feature("cloud_house", new CloudDungeonFeature(DefaultFeatureConfig::deserialize,                 Biomes.OCEAN,
            Biomes.WOODED_BADLANDS_PLATEAU,
            Biomes.DESERT,
            Biomes.DESERT_HILLS,
            Biomes.JUNGLE,
            Biomes.JUNGLE_HILLS,
            Biomes.SWAMP,
            Biomes.SWAMP_HILLS,
            Biomes.ICE_SPIKES,
            Biomes.TAIGA));
    StructureFeature<DefaultFeatureConfig> RUIN = feature("ruin", new RuinFeature(DefaultFeatureConfig::deserialize,
            Biomes.TAIGA,
            Biomes.TAIGA_HILLS,
            Biomes.GIANT_TREE_TAIGA,
            Biomes.GIANT_TREE_TAIGA_HILLS,
            Biomes.SNOWY_TAIGA,
            Biomes.SNOWY_TAIGA_HILLS,
            Biomes.GIANT_SPRUCE_TAIGA,
            Biomes.GIANT_TREE_TAIGA_HILLS,
            Biomes.SNOWY_TAIGA_MOUNTAINS,
            Biomes.DARK_FOREST,
            Biomes.DARK_FOREST_HILLS
    ));

    static StructurePieceType part(String id, StructurePieceType type) {
        return Registry.register(Registry.STRUCTURE_PIECE, new Identifier("unicopia", id), type);
    }

    static <C extends FeatureConfig, F extends StructureFeature<C>> F feature(String name, F feature) {
        Identifier id = new Identifier("unicopia", name);
        return Registry.register(Registry.STRUCTURE_FEATURE, id, Registry.register(Registry.FEATURE, id, feature));
    }

    static void bootstrap() {
        Registry.BIOME.forEach(biome -> {
            Biome.Category category = biome.getCategory();

            if (category == Biome.Category.FOREST) {
                biome.addFeature(GenerationStep.Feature.VEGETAL_DECORATION,
                        Feature.RANDOM_SELECTOR.configure(new RandomFeatureConfig(ImmutableList.of(
                                Feature.NORMAL_TREE.configure(CustomSaplingGenerator.APPLE_TREE.hiveConfig).withChance(0.02F),
                                Feature.FANCY_TREE.configure(CustomSaplingGenerator.APPLE_TREE.fancyConfig).withChance(0.01F)),
                                Feature.NORMAL_TREE.configure(CustomSaplingGenerator.APPLE_TREE.config)
                        ))
                        .createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(0, 0.1F, 3))));
            }
        });
    }
}
