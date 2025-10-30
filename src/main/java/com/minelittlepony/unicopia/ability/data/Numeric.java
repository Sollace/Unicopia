package com.minelittlepony.unicopia.ability.data;

import java.util.Optional;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Numeric (int type) implements Hit {
    private static final Interner<Numeric> INTERNER = Interners.newWeakInterner();
    public static final PacketCodec<ByteBuf, Numeric> CODEC = PacketCodecs.INTEGER.xmap(Numeric::new, Numeric::type);

    public static Optional<Numeric> of(int type) {
        return Optional.of(valueOf(type));
    }

    public static Numeric valueOf(int type) {
        return INTERNER.intern(new Numeric(type));
    }
}