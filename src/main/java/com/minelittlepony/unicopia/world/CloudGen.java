package com.minelittlepony.unicopia.world;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import com.minelittlepony.unicopia.world.structure.AbstractFeature;
import com.minelittlepony.unicopia.world.structure.CloudDungeon;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class CloudGen extends AbstractFeaturesGen {

    private static final List<Biome> BIOMELIST = Arrays.<Biome>asList(
            Biomes.OCEAN,
            Biomes.MESA,
            Biomes.DESERT, Biomes.DESERT_HILLS,
            Biomes.JUNGLE, Biomes.JUNGLE_HILLS,
            Biomes.SWAMP, Biomes.SWAMP_HILLS,
            Biomes.ICE_SPIKES, Biomes.COLD_TAIGA
    );

    public CloudGen() {
        super(8, 32);
    }

    @Override
    public String getStructureName() {
        return "unicopia:clouds";
    }


    @Override
    protected int getRandomSeed() {
        return 143592;
    }

    @Override
    protected boolean canSpawnInBiome(@Nonnull Biome biome) {
        return BIOMELIST.contains(biome);
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new Start(world, rand, chunkX, chunkZ);
    }

    public static class Start extends AbstractFeature.Start {
        public Start() {

        }

        public Start(World world, Random rand, int x, int z) {
            super(world, rand, x, z);
        }

        @Override
        protected void init(World world, Random rand, int x, int z, Biome biome) {
             setRandomHeight(world, rand, 150, world.getActualHeight() - getBoundingBox().getYSize());
        }

        @Override
        protected void addComponents(World world, Random rand, int x, int z, Biome biome) {
            components.add(new CloudDungeon(rand, x * 16, z * 16));
        }
    }

}
