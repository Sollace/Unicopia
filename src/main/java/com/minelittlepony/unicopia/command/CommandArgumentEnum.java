package com.minelittlepony.unicopia.command;

import net.minecraft.util.StringIdentifiable;

public interface CommandArgumentEnum<T extends Enum<T> & CommandArgumentEnum<T>> extends StringIdentifiable {

    @SuppressWarnings("unchecked")
    default T asSelf() {
        return (T)this;
    }

    @Override
    default String asString() {
        return asSelf().name();
    }
}
