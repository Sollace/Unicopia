package com.minelittlepony.unicopia.mixin.datafix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.minelittlepony.unicopia.datafix.SchemasStore;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;

import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

@Mixin(value = DataFixerBuilder.class, remap = false)
public interface DataFixerBuilderAccessor extends SchemasStore {
    @Override
    @Accessor
    Int2ObjectSortedMap<Schema> getSchemas();
}
