package com.minelittlepony.unicopia.datafix;

import com.mojang.datafixers.schemas.Schema;

import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;

public interface SchemasStore {
    Int2ObjectSortedMap<Schema> getSchemas();
}
