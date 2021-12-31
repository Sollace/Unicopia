package com.minelittlepony.unicopia.ability;

import net.minecraft.util.math.MathHelper;

public enum ActivationType {
    NONE,
    TAP,
    DOUBLE_TAP,
    TRIPLE_TAP;

    private static final ActivationType[] VALUES = values();

    public ActivationType getNext() {
        return VALUES[Math.min(VALUES.length - 1, ordinal() + 1)];
    }

    public int getTapCount() {
        return ordinal();
    }

    public static ActivationType of(int id) {
        return VALUES[MathHelper.clamp(id, 0, VALUES.length)];
    }
}
