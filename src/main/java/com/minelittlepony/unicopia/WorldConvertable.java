package com.minelittlepony.unicopia;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public interface WorldConvertable {
    /**
     * Gets the minecraft world
     */
    World asWorld();

    /**
     * Returns true if we're executing on the client.
     */
    default boolean isClient() {
        return asWorld().isClient();
    }

    default <T> RegistryEntry<T> entryFor(RegistryKey<T> key) {
        return asWorld().getRegistryManager().get(key.getRegistryRef()).getEntry(key).orElseThrow();
    }
}
