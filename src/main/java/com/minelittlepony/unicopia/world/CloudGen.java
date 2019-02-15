package com.minelittlepony.unicopia.world;

import java.util.Random;

import com.minelittlepony.unicopia.structure.AbstractFeature;
import com.minelittlepony.unicopia.structure.CloudDungeon;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.StructureStart;

public class CloudGen extends MapGenScatteredFeature {

    @Override
    public String getStructureName() {
        return "unicopia:clouds";
    }

    @Override
    public boolean isSwampHut(BlockPos pos) {
        return false;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new Start(world, rand, chunkX, chunkZ);
    }

    public static class Start extends AbstractFeature.Start {
        public Start() { }

        public Start(World world, Random rand, int x, int z) {
            super(world, rand, x, z);
        }

        public Start(World world, Random rand, int x, int z, Biome biome) {
            super(world, rand, x, z, biome);
        }

        @Override
        protected void addComponents(World world, Random rand, int x, int z, Biome biome) {
            components.add(new CloudDungeon(rand, x * 16, z * 16));
        }
    }
}
