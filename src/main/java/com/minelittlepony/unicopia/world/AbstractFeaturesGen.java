package com.minelittlepony.unicopia.world;

import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public abstract class AbstractFeaturesGen extends MapGenStructure {

    protected int maxDistance;

    protected int minDistance;

    public AbstractFeaturesGen(int min, int max) {
        maxDistance = max;
        minDistance = min;
    }

    public AbstractFeaturesGen(Map<String, String> properties) {
        for (Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().equals("distance")) {
                maxDistance = MathHelper.parseInt(entry.getValue(), maxDistance, 9);
            }
            if (entry.getKey().equals("MinDistance")) {
                minDistance = MathHelper.parseInt(entry.getValue(), minDistance, 9);
            }
        }
    }

    protected abstract int getRandomSeed();

    protected abstract boolean canSpawnInBiome(@Nonnull Biome biome);

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        int i = chunkX;
        int j = chunkZ;

        if (chunkX < 0) {
            chunkX -= maxDistance - 1;
        }

        if (chunkZ < 0) {
            chunkZ -= maxDistance - 1;
        }

        int k = chunkX / maxDistance;
        int l = chunkZ / maxDistance;
        Random random = world.setRandomSeed(k, l, getRandomSeed());

        k = k * maxDistance;
        l = l * maxDistance;
        k = k + random.nextInt(maxDistance - 8);
        l = l + random.nextInt(maxDistance - 8);

        if (i == k && j == l) {
            Biome biome = world.getBiomeProvider().getBiome(new BlockPos(i * 16 + 8, 0, j * 16 + 8));

            return biome != null && canSpawnInBiome(biome);
        }

        return false;
    }

    @Override
    public BlockPos getNearestStructurePos(World world, BlockPos pos, boolean findUnexplored) {
        this.world = world;
        return findNearestStructurePosBySpacing(world, this, pos, maxDistance, 8, getRandomSeed(), false, 100, findUnexplored);
    }
}