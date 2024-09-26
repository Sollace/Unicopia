package com.minelittlepony.unicopia.ability.data;

import java.util.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Numeric (int type) implements Hit {
    public static final PacketCodec<ByteBuf, Numeric> CODEC = PacketCodecs.INTEGER.xmap(Numeric::new, Numeric::type);

    public static Optional<Numeric> of(int type) {
        return Optional.of(new Numeric(type));
    }
}