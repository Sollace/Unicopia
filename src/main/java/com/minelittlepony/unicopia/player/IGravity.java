package com.minelittlepony.unicopia.player;

public interface IGravity {
    boolean isFlying();

    float getFlightExperience();

    float getFlightDuration();

    void setGraviationConstant(float constant);

    float getGravitationConstant();

    default void clearGraviationConstant() {
        setGraviationConstant(Float.NaN);
    }
}
