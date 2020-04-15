package com.minelittlepony.unicopia.structure;

import com.minelittlepony.unicopia.UnicopiaCore;

import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public interface UStructures {
    StructurePieceType CLOUD_HOUSE_PART = part("cloud_house", CloudDungeonFeature.Piece::new);
    StructurePieceType RUIN_PART = part("ruin", RuinFeature.Piece::new);

    StructureFeature<DefaultFeatureConfig> CLOUD_HOUSE = feature("cloud_house", new CloudDungeonFeature(DefaultFeatureConfig::deserialize));
    StructureFeature<DefaultFeatureConfig> RUIN = feature("ruin", new RuinFeature(DefaultFeatureConfig::deserialize));

    static StructurePieceType part(String id, StructurePieceType type) {
        return Registry.register(Registry.STRUCTURE_PIECE, new Identifier(UnicopiaCore.MODID, id), type);
    }

    static <C extends FeatureConfig, F extends Feature<C>> F feature(String id, F feature) {
        return Registry.register(Registry.FEATURE, new Identifier(UnicopiaCore.MODID, id), feature);
     }

    static void bootstrap() { }
}
