package com.minelittlepony.unicopia;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public interface WorldConvertable {
    /**
     * Gets the minecraft world
     */
    World asWorld();

    /**
     * Gets the minecraft server world
     */
    default ServerWorld asServerWorld() {
        return (ServerWorld)asWorld();
    }

    /**
     * Returns true if we're executing on the client.
     */
    default boolean isClient() {
        return asWorld().isClient();
    }

    default <T> RegistryEntry<T> entryFor(RegistryKey<T> key) {
        return asWorld().getRegistryManager().getOrThrow(key.getRegistryRef()).getEntry(key.getValue()).orElseThrow();
    }
}
