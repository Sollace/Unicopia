package com.minelittlepony.unicopia.server.world.gen;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.server.world.UWorldGen;
import com.mojang.serialization.Codec;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldView;

public class CaveCarvingStructureProcessor extends StructureProcessor {
    public static final Codec<CaveCarvingStructureProcessor> CODEC = Codec.unit(new CaveCarvingStructureProcessor());

    @Override
    protected StructureProcessorType<?> getType() {
        return UWorldGen.SURFACE_GROWTH_STRUCTURE_PROCESSOR;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(
        WorldView world,
        BlockPos pos,
        BlockPos pivot,
        StructureTemplate.StructureBlockInfo originalBlockInfo,
        StructureTemplate.StructureBlockInfo currentBlockInfo,
        StructurePlacementData data
    ) {
        int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, currentBlockInfo.pos().getX(), currentBlockInfo.pos().getZ());
        return currentBlockInfo.pos().getY() > topY && world.isAir(currentBlockInfo.pos()) ? null : currentBlockInfo;
    }
}
