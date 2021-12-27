package com.minelittlepony.unicopia;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class AwaitTickQueue {
    public static void scheduleTask(World reference, Consumer<World> task, int ticksLater) {
        if (reference instanceof ServerWorld) {
            CompletableFuture.runAsync(() -> {
                task.accept(reference);
            }, CompletableFuture.delayedExecutor(ticksLater * 100, TimeUnit.MILLISECONDS, reference.getServer()));
        }
    }

    public static void scheduleTask(World reference, Consumer<World> task) {
        if (reference instanceof ServerWorld) {
            CompletableFuture.runAsync(() -> {
                task.accept(reference);
            }, reference.getServer());
        }
    }
}
