package com.minelittlepony.unicopia.entity.player;

import net.minecraft.util.math.Vec3d;

/**
 * Interface for controlling flight.
 */
public interface Motion {
    /**
     * True if we're currently flying.
     */
    boolean isFlying();

    boolean isGliding();

    boolean isDiving();

    boolean isRainbooming();

    float getWingAngle();

    PlayerDimensions getDimensions();

    Vec3d getClientVelocity();
}
