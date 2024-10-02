package com.minelittlepony.unicopia.mixin.datafix;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;

import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

@Mixin(value = DataFixerBuilder.class, remap = false)
public interface DataFixerBuilderAccessor {
    @Accessor
    Int2ObjectSortedMap<Schema> getSchemas();

    @Nullable
    default Schema getSchema(final int version, final int subVersion) {
        return getSchemas().get(DataFixUtils.makeKey(version, subVersion));
    }
}
