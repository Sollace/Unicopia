package com.minelittlepony.unicopia.network;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/**
 * Sent by the server to update block destruction progress on the client.
 */
public record MsgBlockDestruction(Long2ObjectMap<Float> destructions) {
    public static final PacketCodec<ByteBuf, MsgBlockDestruction> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(Long2ObjectOpenHashMap::new, PacketCodecs.VAR_LONG, PacketCodecs.FLOAT), MsgBlockDestruction::destructions,
            MsgBlockDestruction::new
    );
}
