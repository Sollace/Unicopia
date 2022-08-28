package com.minelittlepony.unicopia.client.sound;

import java.util.*;
import java.util.concurrent.*;

public class BufferedExecutor {
    private static final Executor EXECUTOR = CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS);
    private static final Map<Integer, CompletableFuture<?>> PENDING_EXECUTIONS = new HashMap<>();

    public static void bufferExecution(Object obj, Runnable action) {
        synchronized (PENDING_EXECUTIONS) {
            PENDING_EXECUTIONS.computeIfAbsent(obj.hashCode(), hash -> {
                return CompletableFuture.runAsync(action, EXECUTOR).thenRun(() -> {
                    synchronized (PENDING_EXECUTIONS) {
                        PENDING_EXECUTIONS.remove(hash);
                    }
                });
            });
        }
    }
}
