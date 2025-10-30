package com.minelittlepony.unicopia.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record MsgEntityStatus(int entityId, int status) {
    public static final int USE_TOTEM_OF_DYING = 1;

    public static final PacketCodec<RegistryByteBuf, MsgEntityStatus> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, MsgEntityStatus::entityId,
            PacketCodecs.INTEGER, MsgEntityStatus::status,
            MsgEntityStatus::new
    );
}
