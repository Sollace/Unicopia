package com.minelittlepony.unicopia;

public enum Availability {
    DEFAULT,
    COMMANDS,
    NONE;

    public boolean isSelectable() {
        return this == DEFAULT;
    }

    public boolean isGrantable() {
        return this != NONE;
    }
}
