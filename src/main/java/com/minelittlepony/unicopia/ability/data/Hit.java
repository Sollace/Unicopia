package com.minelittlepony.unicopia.ability.data;

import java.util.Optional;
import net.minecraft.network.PacketByteBuf;

public interface Hit {
    Optional<Hit> INSTANCE = Optional.of(new Hit() {});
    Serializer<Hit> SERIALIZER = new Serializer<>(buf -> INSTANCE.get(), (buf, t) -> {});

    static Optional<Hit> of(boolean value) {
        return value ? INSTANCE : Optional.empty();
    }

    public record Serializer<T extends Hit> (
            PacketByteBuf.PacketReader<T> read,
            PacketByteBuf.PacketWriter<T> write) {
    }
}