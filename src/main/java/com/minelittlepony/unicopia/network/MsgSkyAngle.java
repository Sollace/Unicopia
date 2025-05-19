package com.minelittlepony.unicopia.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record MsgSkyAngle (
        float tangentalSkyAngle
    ) {
    public static final PacketCodec<ByteBuf, MsgSkyAngle> PACKET_CODEC = PacketCodecs.FLOAT.xmap(MsgSkyAngle::new, MsgSkyAngle::tangentalSkyAngle);
}
