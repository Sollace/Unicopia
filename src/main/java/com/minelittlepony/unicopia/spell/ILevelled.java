package com.minelittlepony.unicopia.spell;

public interface ILevelled {

    /**
     * Maximum level this spell can reach or -1 for unlimited.
     * <br>
     * If a gem goes past this level it is more likely to explode.
     */
    default int getMaxLevel() {
        return 0;
    }

    default boolean canLevelUp() {
        int max = getMaxLevel();
        return max < 0 || getCurrentLevel() < max;
    }

    int getCurrentLevel();

    void setCurrentLevel(int level);

    default void addLevels(int levels) {
        setCurrentLevel(getCurrentLevel() + levels);
    }
}
