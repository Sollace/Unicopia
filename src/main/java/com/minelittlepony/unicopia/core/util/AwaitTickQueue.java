package com.minelittlepony.unicopia.core.util;

import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.minelittlepony.unicopia.core.UnicopiaCore;
import net.minecraft.world.World;

public class AwaitTickQueue {

    private static final Queue<Consumer<World>> tickTasks = Queues.newArrayDeque();
    private static List<DelayedTask> delayedTasks = Lists.newArrayList();

    private static final Object locker = new Object();

    public static void enqueueTask(Consumer<World> task) {
        synchronized (locker) {
            tickTasks.add(task);
        }
    }

    public static void scheduleTask(Consumer<World> task, int ticksLater) {
        synchronized (locker) {
            delayedTasks.add(new DelayedTask(task, ticksLater));
        }
    }

    public void onUpdate(World world) {
        synchronized (locker) {
            delayedTasks = delayedTasks.stream().filter(DelayedTask::tick).collect(Collectors.toList());

            Consumer<World> task;
            while ((task = tickTasks.poll()) != null) {
                try {
                    task.accept(world);
                } catch (Exception e) {
                    UnicopiaCore.LOGGER.error(e);
                }
            }
        }
    }

    private static class DelayedTask {
        final Consumer<World> task;

        int ticksDelay;

        DelayedTask(Consumer<World> task, int ticks) {
            this.task = task;
            this.ticksDelay = ticks;
        }

        boolean tick() {
            if (ticksDelay-- <= 0) {
                tickTasks.add(task);

                return false;
            }

            return true;
        }
    }

}
