package com.minelittlepony.unicopia.ability.magic;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

/**
 * Object with levelling capabilities.
 */
public interface Levelled {
    LevelStore ZERO = of(0, 1);

    static LevelStore of(IntSupplier getter, IntConsumer setter, IntSupplier max) {
        return new LevelStore() {
            @Override
            public int get() {
                return getter.getAsInt();
            }

            @Override
            public void set(int level) {
                setter.accept(level);
            }

            @Override
            public int getMax() {
                return max.getAsInt();
            }
        };
    }

    static LevelStore copyOf(LevelStore store) {
        return of(store.get(), store.getMax());
    }

    static LevelStore fromNbt(NbtCompound compound) {
        int max = Math.max(1, compound.getInt("max"));
        int value = MathHelper.clamp(compound.getInt("value"), 0, max);
        return of(value, max);
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
            if (getMax() == 0) {
                return max;
            }
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
