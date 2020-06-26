package com.minelittlepony.unicopia.equine.player;

public interface MagicReserves {
    /**
     * Gets the amount of exertion this player has put toward any given activity.
     * This is simillar to tiredness.
     */
    float getExertion();

    /**
     * Sets the player's exertion level.
     */
    void setExertion(float exertion);

    /**
     * Adds player tiredness.
     */
    default void addExertion(int exertion) {
        setExertion(getExertion() + exertion/100F);
    }

    /**
     * Gets the amount of excess energy the player has.
     * This is increased by eating sugar.
     */
    float getEnergy();

    /**
     * Sets the player's energy level.
     */
    void setEnergy(float energy);

    /**
     * Adds energy to the player's existing energy level.
     */
    default void addEnergy(int energy) {
        setEnergy(getEnergy() + energy / 100F);
    }

}
