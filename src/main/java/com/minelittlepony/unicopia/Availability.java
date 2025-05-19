package com.minelittlepony.unicopia;

import java.util.Locale;

import net.minecraft.util.StringIdentifiable;

public enum Availability implements StringIdentifiable {
    DEFAULT,
    COMMANDS,
    NONE;

    @SuppressWarnings("deprecation")
    public static final EnumCodec<Availability> CODEC = StringIdentifiable.createCodec(Availability::values);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String asString() {
        return name;
    }

    public boolean isSelectable() {
        return this == DEFAULT;
    }

    public boolean isGrantable() {
        return this != NONE;
    }
}
