package com.minelittlepony.unicopia.structure;

import com.google.common.collect.ImmutableList;

import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
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

    StructureFeature<DefaultFeatureConfig> CLOUD_HOUSE = feature("cloud_house", new CloudDungeonFeature(DefaultFeatureConfig::deserialize));
    StructureFeature<DefaultFeatureConfig> RUIN = feature("ruin", new RuinFeature(DefaultFeatureConfig::deserialize));

    static StructurePieceType part(String id, StructurePieceType type) {
        return Registry.register(Registry.STRUCTURE_PIECE, new Identifier("unicopia", id), type);
    }

    static <C extends FeatureConfig, F extends Feature<C>> F feature(String id, F feature) {
        return Registry.register(Registry.FEATURE, new Identifier("unicopia", id), feature);
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
                        .createDecoratedFeature(Decorator.COUNT_EXTRA_HEIGHTMAP.configure(new CountExtraChanceDecoratorConfig(10, 0.1F, 1))));
            }
        });
    }
}
