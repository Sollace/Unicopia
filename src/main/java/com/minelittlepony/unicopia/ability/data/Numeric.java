package com.minelittlepony.unicopia.ability.data;

import java.util.Optional;

public record Numeric (int type) implements Hit {
    public static final Serializer<Numeric> SERIALIZER = new Serializer<>(
            buf -> new Numeric(buf.readInt()),
            (buf, t) -> buf.writeInt(t.type()));

    public static Optional<Numeric> of(int type) {
        return Optional.of(new Numeric(type));
    }
}