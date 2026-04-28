package com.minelittlepony.unicopia.mixin.server;


import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;

@Mixin(StructurePool.class)
public interface MixinStructurePool {
    @Accessor
    ObjectArrayList<StructurePoolElement> getElements();

    @Accessor
    @Mutable
    void setElements(ObjectArrayList<StructurePoolElement> elements);

    @Accessor("elementWeights")
    List<Pair<StructurePoolElement, Integer>> getElementCounts();

    @Accessor("elementWeights")
    @Mutable
    void setElementCounts(List<Pair<StructurePoolElement, Integer>> elementWeights);
}
