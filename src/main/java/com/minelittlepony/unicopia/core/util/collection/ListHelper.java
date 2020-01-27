package com.minelittlepony.unicopia.core.util.collection;

import java.util.List;

import net.minecraft.world.biome.Biome.SpawnEntry;

public final class ListHelper {
    public static void addifAbsent(List<SpawnEntry> entries, SpawnEntry entry) {
        if (!entries.contains(entry)) {
            entries.add(entry);
        }
    }
}
