package com.minelittlepony.unicopia.player;

public interface IGravity extends IFlyingPredicate {
    boolean isFlying();

    float getFlightExperience();

    float getFlightDuration();
}
