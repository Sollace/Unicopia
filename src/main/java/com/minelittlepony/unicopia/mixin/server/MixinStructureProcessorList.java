package com.minelittlepony.unicopia.mixin.server;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorList;

@Mixin(StructureProcessorList.class)
public interface MixinStructureProcessorList {
    @Accessor
    @Mutable
    void setList(List<StructureProcessor> list);
}
