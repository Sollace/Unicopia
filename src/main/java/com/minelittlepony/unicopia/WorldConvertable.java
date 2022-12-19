package com.minelittlepony.unicopia;

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
}
