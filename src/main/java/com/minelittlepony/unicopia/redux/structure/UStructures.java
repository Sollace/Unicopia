package com.minelittlepony.unicopia.redux.structure;

import com.minelittlepony.unicopia.core.UnicopiaCore;

import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UStructures {

    StructurePieceType CLOUD_HOUSE = register(CloudDungeonFeature.Piece::new, new Identifier(UnicopiaCore.MODID, "cloud_house"));
    StructurePieceType RUIN = register(RuinFeature.Piece::new, new Identifier(UnicopiaCore.MODID, "ruin"));


    static StructurePieceType register(StructurePieceType type, Identifier id) {
        return Registry.register(Registry.STRUCTURE_PIECE, id, type);
    }

    static void bootstrap() {}
}
