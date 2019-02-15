package com.minelittlepony.unicopia.world;

import java.util.Queue;

import com.google.common.collect.Queues;
import com.minelittlepony.jumpingcastle.Exceptions;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.IChunkGenerator;

public class UWorld {

    private static final UWorld instance = new UWorld();

    public static UWorld instance() {
        return instance;
    }

    private static final Queue<Runnable> tickTasks = Queues.newArrayDeque();

    private static final Object locker = new Object();

    public static void tick(World world) {
        synchronized (locker) {
            Runnable task;
            while ((task = tickTasks.poll()) != null) {
                Exceptions.logged(task, Unicopia.log);
            }
        }
    }

    public static void enqueueTask(Runnable task) {
        synchronized (locker) {
            tickTasks.add(task);
        }
    }

    private CloudGen cloudStructureGen = new CloudGen();

    public void generateStructures(World world, int chunkX, int chunkZ, IChunkGenerator gen) {
        if (gen instanceof ChunkGeneratorOverworld) {
            if (world.getWorldInfo().isMapFeaturesEnabled()) {
                ChunkPos pos = new ChunkPos(chunkX, chunkZ);

                cloudStructureGen.generateStructure(world, world.rand, pos);
            }
        }
    }
}
