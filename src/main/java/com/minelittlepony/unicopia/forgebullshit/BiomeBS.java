package com.minelittlepony.unicopia.forgebullshit;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.world.biome.Biome;

/**
 * Provides methods and apis that forge seems to be sorely lacking.
 */
@Deprecated
public class BiomeBS {

    /**
     * Gets the biome type associated with a given biome.
     */
    public static Optional<BiomeType> getBiomeType(Biome biome) {
        return Lists.newArrayList(BiomeManager.BiomeType.values()).stream().filter(type ->
            BiomeManager.getBiomes(type).stream().filter(entry ->
                    entry.biome.equals(biome)
                ).findFirst()
                .isPresent()
        ).findFirst();
    }

    /**
     * Adds a spawn entry for the specified entity if one does not already exist.
     */
    public static void addSpawnEntry(Biome biome, EnumCreatureType list, Class<? extends LivingEntity> type, Function<Biome, SpawnListEntry> func) {
        List<SpawnListEntry> entries = biome.getSpawnableList(list);

        entries.stream().filter(p -> p.entityClass == type).findFirst().orElseGet(() -> {
            entries.add(func.apply(biome));
            return null;
        });
    }
}
