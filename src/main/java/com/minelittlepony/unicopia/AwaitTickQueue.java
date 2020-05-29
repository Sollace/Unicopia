package com.minelittlepony.unicopia;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.world.World;

public class AwaitTickQueue {
    private static final Object LOCKER = new Object();
    private static List<Entry> PENDING_TASKS = new ArrayList<>();

    public static void scheduleTask(World reference, Consumer<World> task, int ticksLater) {
        if (!reference.isClient) {
            synchronized (LOCKER) {
                PENDING_TASKS.add(new Entry(reference, task, ticksLater));
            }
        }
    }

    static void tick(World world) {
        if (!world.isClient) {
            synchronized (LOCKER) {
                final Queue<Entry> tasks = new ArrayDeque<>();
                PENDING_TASKS = PENDING_TASKS.stream().filter(e -> e.tick(world, tasks)).collect(Collectors.toList());
                tasks.forEach(e -> e.run(world));
            }
        }
    }

    private static final class Entry {
        private final Consumer<World> task;
        private final WeakReference<World> world;
        private int ticks;

        Entry(World world, Consumer<World> task, int ticks) {
            this.world = new WeakReference<>(world);
            this.task = task;
            this.ticks = ticks;
        }

        boolean tick(World world, Queue<Entry> tasks) {
            World w = this.world.get();
            return w != null && (w != world || ticks-- > 0 || !tasks.add(this));
        }

        void run(World world) {
            if (this.world.get() == world) {
                try {
                    task.accept(world);
                } catch (Exception e) {
                    Unicopia.LOGGER.error(e);
                }
            }
        }
    }
}
