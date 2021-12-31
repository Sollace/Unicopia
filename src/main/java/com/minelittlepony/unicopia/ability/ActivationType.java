package com.minelittlepony.unicopia.ability;

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
}