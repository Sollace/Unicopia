package com.minelittlepony.unicopia;

import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import net.minecraft.world.World;

public class AwaitTickQueue {

    private static final Queue<Consumer<World>> SCHEDULED_TASKS = Queues.newArrayDeque();
    private static List<DelayedTask> DELAYED_TASKS = Lists.newArrayList();

    private static final Object LOCKER = new Object();

    public static void enqueueTask(Consumer<World> task) {
        synchronized (LOCKER) {
            SCHEDULED_TASKS.add(task);
        }
    }

    public static void scheduleTask(Consumer<World> task, int ticksLater) {
        synchronized (LOCKER) {
            DELAYED_TASKS.add(new DelayedTask(task, ticksLater));
        }
    }

    static void tick(World world) {
        synchronized (LOCKER) {
            DELAYED_TASKS = DELAYED_TASKS.stream().filter(DelayedTask::tick).collect(Collectors.toList());

            Consumer<World> task;
            while ((task = SCHEDULED_TASKS.poll()) != null) {
                try {
                    task.accept(world);
                } catch (Exception e) {
                    Unicopia.LOGGER.error(e);
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
                SCHEDULED_TASKS.add(task);

                return false;
            }

            return true;
        }
    }

}
