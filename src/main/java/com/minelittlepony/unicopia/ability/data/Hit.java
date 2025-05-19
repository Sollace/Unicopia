package com.minelittlepony.unicopia.ability.data;

import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public interface Hit {
    Optional<Hit> INSTANCE = Optional.of(new Hit() {});
    PacketCodec<PacketByteBuf, Hit> CODEC = PacketCodec.unit(INSTANCE.get());

    static Optional<Hit> of(boolean value) {
        return value ? INSTANCE : Optional.empty();
    }
}