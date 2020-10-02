package com.minelittlepony.unicopia.ability.magic;

/**
 * Object with levelling capabilities.
 */
public interface Levelled {
    static LevelStore fixed(int level) {
        return new LevelStore() {
            @Override
            public int get() {
                return level;
            }

            @Override
            public void set(int level) {
            }

            @Override
            public int getMax() {
                return get();
            }

        };
    }

    LevelStore getLevel();

    interface LevelStore {
        int getMax();

        int get();

        void set(int level);

        default boolean canLevelUp() {
            int max = getMax();
            return max < 0 || get() < max;
        }

        default void add(int levels) {
            set(get() + levels);
        }
    }
}
