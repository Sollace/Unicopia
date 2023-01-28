package com.minelittlepony.unicopia;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public interface AwaitTickQueue {
    static void scheduleTask(World reference, Consumer<World> task, int ticksLater) {
        if (reference instanceof ServerWorld serverWorld) {
            CompletableFuture.runAsync(() -> {
                task.accept(serverWorld);
            }, CompletableFuture.delayedExecutor(ticksLater * 100, TimeUnit.MILLISECONDS, serverWorld.getServer()));
        }
    }

    static void scheduleTask(World reference, Consumer<World> task) {
        if (reference instanceof ServerWorld serverWorld) {
            CompletableFuture.runAsync(() -> {
                task.accept(serverWorld);
            }, CompletableFuture.delayedExecutor(1, TimeUnit.MILLISECONDS, serverWorld.getServer()));
        }
    }
}
