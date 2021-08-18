package com.minelittlepony.unicopia.entity;

public interface Leaner {
    float getLeaningPitch();

    void setLeaningPitch(float pitch);

    float getLastLeaningPitch();

    void setLastLeaningPitch(float pitch);

    default void copyFrom(Leaner other) {
        setLeaningPitch(other.getLeaningPitch());
        setLastLeaningPitch(other.getLastLeaningPitch());
    }
}
