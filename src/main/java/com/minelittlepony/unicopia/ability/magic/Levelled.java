package com.minelittlepony.unicopia.ability.magic;

import java.util.function.IntSupplier;

import net.minecraft.nbt.NbtCompound;

/**
 * Object with levelling capabilities.
 */
public interface Levelled {
    LevelStore EMPTY = fixed(0);

    static LevelStore fixed(int level) {
        return of(() -> level);
    }

    static LevelStore of(IntSupplier supplier) {
        return new LevelStore() {
            @Override
            public int get() {
                return supplier.getAsInt();
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

    static LevelStore copyOf(LevelStore store) {
        return of(store.get(), store.getMax());
    }

    static LevelStore fromNbt(NbtCompound compound) {
        return of(compound.getInt("value"), compound.getInt("max"));
    }

    static LevelStore of(int level, int max) {
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
                return max;
            }
        };
    }

    LevelStore getLevel();

    LevelStore getCorruption();

    interface LevelStore {
        int getMax();

        int get();

        void set(int level);

        default float getScaled(float max) {
            return ((float)get() / getMax()) * max;
        }

        default boolean canLevelUp() {
            int max = getMax();
            return max < 0 || get() < max;
        }

        default void add(int levels) {
            set(get() + levels);
        }

        default NbtCompound toNbt() {
            NbtCompound compound = new NbtCompound();
            compound.putInt("value", get());
            compound.putInt("max", getMax());
            return compound;
        }
    }
}
