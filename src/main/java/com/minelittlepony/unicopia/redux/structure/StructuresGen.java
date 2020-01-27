package com.minelittlepony.unicopia.redux.structure;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class StructuresGen extends AbstractFeaturesGen {

    private static final List<Biome> BIOMELIST = Arrays.<Biome>asList(
            Biomes.TAIGA,
            Biomes.TAIGA_HILLS,
            Biomes.EXTREME_HILLS_WITH_TREES,
            Biomes.COLD_TAIGA,
            Biomes.COLD_TAIGA_HILLS,
            Biomes.MUTATED_TAIGA,
            Biomes.MUTATED_TAIGA_COLD,
            Biomes.MUTATED_EXTREME_HILLS_WITH_TREES,
            Biomes.ROOFED_FOREST
    );

    public StructuresGen() {
        super(8, 16);
    }

    @Override
    public String getStructureName() {
        return "unicopia:ruins";
    }

    @Override
    protected int getRandomSeed() {
        return 39548;
    }

    @Override
    protected boolean canSpawnInBiome(Biome biome) {
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
        protected void addComponents(World world, Random rand, int x, int z, Biome biome) {
            components.add(new GroundDungeon(rand, x * 16, z * 16));
        }
    }
}
