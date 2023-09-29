package com.minelittlepony.unicopia.network;

import com.sollace.fabwork.api.packets.Packet;
import net.minecraft.network.PacketByteBuf;

/**
 * Sent to the client when an ability fails its server-side activation checks.
 */
public final class MsgCancelPlayerAbility implements Packet {
    static final MsgCancelPlayerAbility INSTANCE = new MsgCancelPlayerAbility();

    static MsgCancelPlayerAbility read(PacketByteBuf buffer) {
        return INSTANCE;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) { }
}
