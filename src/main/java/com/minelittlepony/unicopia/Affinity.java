package com.minelittlepony.unicopia;

import net.minecraft.util.Formatting;

public enum Affinity {
    GOOD(Formatting.BLUE, -1),
    NEUTRAL(Formatting.WHITE, 0),
    BAD(Formatting.RED, 1);

    private final Formatting color;

    private final int corruption;

    public static final Affinity[] VALUES = values();

    Affinity(Formatting color, int corruption) {
        this.color = color;
        this.corruption = corruption;
    }

    public Formatting getColor() {
        return color;
    }

    public String getTranslationKey() {
        return this == BAD ? "curse" : "spell";
    }

    public int getCorruption() {
        return corruption;
    }

    public boolean isNeutral() {
        return this == NEUTRAL;
    }

    public boolean alignsWith(Affinity other) {
        return isNeutral() || other.isNeutral() || this == other;
    }

    public static Affinity of(int ordinal, Affinity fallback) {
        return ordinal < 0 || ordinal >= VALUES.length ? fallback : VALUES[ordinal];
    }
}
