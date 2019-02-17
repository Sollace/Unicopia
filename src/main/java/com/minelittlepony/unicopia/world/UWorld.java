package com.minelittlepony.unicopia.world;

import java.util.Queue;
import java.util.Random;

import com.google.common.collect.Queues;
import com.minelittlepony.jumpingcastle.Exceptions;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.structure.CloudDungeon;
import com.minelittlepony.unicopia.structure.GroundDungeon;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class UWorld implements IWorldGenerator {

    private static final UWorld instance = new UWorld();

    public static UWorld instance() {
        return instance;
    }

    private static final Queue<Runnable> tickTasks = Queues.newArrayDeque();

    private static final Object locker = new Object();

    public static void enqueueTask(Runnable task) {
        synchronized (locker) {
            tickTasks.add(task);
        }
    }

    private final BlockInteractions blocks = new BlockInteractions();

    private final CloudGen cloudsGen = new CloudGen();
    private final StructuresGen structuresGen = new StructuresGen();

    public void init() {
        GameRegistry.registerWorldGenerator(this, 1);

        MapGenStructureIO.registerStructure(CloudGen.Start.class, "unicopia:clouds");
        MapGenStructureIO.registerStructure(StructuresGen.Start.class, "unicopia:ruins");
        MapGenStructureIO.registerStructureComponent(CloudDungeon.class, "unicopia:cloud_dungeon");
        MapGenStructureIO.registerStructureComponent(GroundDungeon.class, "unicopia:ground_dungeon");
    }

    public void onUpdate(World world) {
        synchronized (locker) {
            Runnable task;
            while ((task = tickTasks.poll()) != null) {
                Exceptions.logged(task, Unicopia.log);
            }
        }
    }

    public BlockInteractions getBlocks() {
        return blocks;
    }

    public void generateStructures(World world, int chunkX, int chunkZ, IChunkGenerator gen) {
        if (gen instanceof ChunkGeneratorOverworld) {
            if (world.getWorldInfo().isMapFeaturesEnabled()) {
                ChunkPos pos = new ChunkPos(chunkX, chunkZ);

                cloudsGen.generateStructure(world, world.rand, pos);
                structuresGen.generateStructure(world, world.rand, pos);
            }
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (chunkGenerator instanceof ChunkGeneratorOverworld) {
            if (world.getWorldInfo().isMapFeaturesEnabled()) {
                ChunkPrimer primer = new ChunkPrimer();

                cloudsGen.generate(world, chunkX, chunkZ, primer);
                structuresGen.generate(world, chunkX, chunkZ, primer);
            }
        }
    }
}
