package com.minelittlepony.unicopia.entity;

/**
 * Interface for controlling the gravity applicable to a specific entity.
 */
public interface IGravity {

    void setGraviationConstant(float constant);

    float getGravitationConstant();

    default void clearGraviationConstant() {
        setGraviationConstant(0);
    }
}
