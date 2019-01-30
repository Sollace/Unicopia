package com.minelittlepony.unicopia.forgebullshit;

import java.util.Optional;

import com.google.common.collect.Lists;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeType;

/**
 * Provides methods and apis that forge seems to be sorely lacking.
 */
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
}
