package com.minelittlepony.unicopia.redux.structure;

import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.AbstractTempleFeature;
import net.minecraft.world.gen.feature.FeatureConfig;

public abstract class BiomeWhitelistedFeature<C extends FeatureConfig> extends AbstractTempleFeature<C> {

    public BiomeWhitelistedFeature(Function<Dynamic<?>, ? extends C> function_1) {
        super(function_1);
    }

    @Override
    public boolean shouldStartAt(ChunkGenerator<?> generator, Random rand, int x, int z) {
       ChunkPos pos = getStart(generator, rand, x, z, 0, 0);
       if (x == pos.x && z == pos.z) {
          Biome biome = generator.getBiomeSource().getBiome(new BlockPos(x * 16 + 9, 0, z * 16 + 9));

          if (canSpawnInBiome(biome)) {
             return true;
          }
       }

       return false;
    }

    protected abstract boolean canSpawnInBiome(@Nonnull Biome biome);

}
