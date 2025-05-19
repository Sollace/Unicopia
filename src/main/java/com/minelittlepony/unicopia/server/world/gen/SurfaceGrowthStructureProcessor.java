package com.minelittlepony.unicopia.server.world.gen;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.server.world.UWorldGen;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

public class SurfaceGrowthStructureProcessor extends StructureProcessor {
    public static final MapCodec<SurfaceGrowthStructureProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        RuleTest.TYPE_CODEC.fieldOf("input_predicate").forGetter(rule -> rule.inputPredicate),
        BlockState.CODEC.fieldOf("output_state").forGetter(rule -> rule.outputState)
    ).apply(instance, SurfaceGrowthStructureProcessor::new));

    private final RuleTest inputPredicate;
    private final BlockState outputState;

    public SurfaceGrowthStructureProcessor(RuleTest inputPredicate, BlockState outputState) {
        this.inputPredicate = inputPredicate;
        this.outputState = outputState;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return UWorldGen.SURFACE_GROWTH_STRUCTURE_PROCESSOR;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<StructureTemplate.StructureBlockInfo> reprocess(
        ServerWorldAccess world,
        BlockPos pos,
        BlockPos pivot,
        List<StructureTemplate.StructureBlockInfo> originalBlockInfos,
        List<StructureTemplate.StructureBlockInfo> currentBlockInfos,
        StructurePlacementData data
    ) {
        Map<BlockPos, StructureTemplate.StructureBlockInfo> positionalInfos = currentBlockInfos.stream().collect(Collectors.toMap(
                StructureTemplate.StructureBlockInfo::pos,
                Function.identity()
        ));

        return currentBlockInfos.stream().map(currentBlockInfo -> {
            StructureTemplate.StructureBlockInfo aboveBlockInfo = positionalInfos.get(currentBlockInfo.pos().up());
            BlockState currentState = aboveBlockInfo == null ? world.getBlockState(currentBlockInfo.pos().up()) : aboveBlockInfo.state();
            if ((currentState.isAir() || currentState.isReplaceable())
                    && inputPredicate.test(currentBlockInfo.state(), Random.create(MathHelper.hashCode(currentBlockInfo.pos())))) {
                return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), outputState, currentBlockInfo.nbt());
            }
            return currentBlockInfo;
        }).collect(Collectors.toList());
    }
}
