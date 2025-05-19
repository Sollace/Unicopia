package com.minelittlepony.unicopia.ability;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.MathHelper;

public enum ActivationType {
    NONE,
    TAP,
    DOUBLE_TAP,
    TRIPLE_TAP;

    private static final ActivationType[] VALUES = values();
    public static final PacketCodec<ByteBuf, ActivationType> PACKET_CODEC = PacketCodecs.indexed(i -> VALUES[i], ActivationType::ordinal);

    public ActivationType getNext() {
        return VALUES[Math.min(VALUES.length - 1, ordinal() + 1)];
    }

    public int getTapCount() {
        return ordinal();
    }

    public boolean isResult() {
        return this != NONE;
    }

    @Deprecated
    public static ActivationType of(int id) {
        return VALUES[MathHelper.clamp(id, 0, VALUES.length)];
    }
}
