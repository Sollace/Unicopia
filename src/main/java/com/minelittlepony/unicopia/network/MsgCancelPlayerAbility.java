package com.minelittlepony.unicopia.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

/**
 * Sent to the client when an ability fails its server-side activation checks.
 */
public final class MsgCancelPlayerAbility {
    public static final MsgCancelPlayerAbility INSTANCE = new MsgCancelPlayerAbility();
    public static final PacketCodec<ByteBuf, MsgCancelPlayerAbility> PACKET_CODEC = PacketCodec.unit(INSTANCE);
}
