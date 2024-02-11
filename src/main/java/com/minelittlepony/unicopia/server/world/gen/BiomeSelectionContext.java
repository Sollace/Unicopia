package com.minelittlepony.unicopia.server.world.gen;

import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

public interface BiomeSelectionContext {
    RegistryKey<Biome> biomeKey();

    SplittableBiomeCoordinate referenceFrame();

    @Nullable
    RegistryKey<Biome> addOverride(SplittableBiomeCoordinate coordinate, RegistryKey<Biome> biome);

    public record SplittableBiomeCoordinate(
            SplitableParameterRange temperature,
            SplitableParameterRange humidity,
            SplitableParameterRange continentalness,
            SplitableParameterRange erosion,
            SplitableParameterRange depth,
            SplitableParameterRange weirdness,
            long offset) {
    }

    public interface SplitableParameterRange {
        SplittableBiomeCoordinate splitAbove(float midpoint);

        SplittableBiomeCoordinate splitBelow(float midpoint);
    }
}
