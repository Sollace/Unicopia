package com.minelittlepony.unicopia.world;

import java.util.Queue;

import com.google.common.collect.Queues;
import com.minelittlepony.jumpingcastle.Exceptions;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.world.World;

public class UWorld {

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
}
