package com.minelittlepony.unicopia.world;

import java.util.Random;

import com.minelittlepony.unicopia.structure.AbstractFeature;
import com.minelittlepony.unicopia.structure.GroundDungeon;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureStart;

public class StructuresGen extends MapGenScatteredFeature {

    @Override
    public String getStructureName() {
        return "unicopia:ruins";
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
        public Start() {

        }

        public Start(World world, Random rand, int x, int z) {
            super(world, rand, x, z);
        }

        @Override
        protected void addComponents(World world, Random rand, int x, int z, Biome biome) {
            components.add(new GroundDungeon(rand, x * 16, z * 16));
        }

        @Override
        public void generateStructure(World world, Random rand, StructureBoundingBox bounds) {
            super.generateStructure(world, rand, bounds);
        }
    }
}
