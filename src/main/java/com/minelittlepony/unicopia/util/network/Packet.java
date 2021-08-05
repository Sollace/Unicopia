package com.minelittlepony.unicopia.util.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

/**
 * Represents a message that can be either send from the client to the server or back.
 */
public interface Packet<P extends PlayerEntity> {
    /**
     * Called to handle this packet on the receiving end.
     *
     * @param sender The player who initially sent this packet.
     */
    void handle(P sender);

    /**
     * Writes this packet to the supplied buffer prior to transmission.
     *
     * @param buffer The buffer to write to.
     */
    void toBuffer(PacketByteBuf buffer);

    /**
     * Writes this packet to a new buffer.
     *
     * @return The resulting buffer for transmission
     */
    default PacketByteBuf toBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        toBuffer(buf);
        return buf;
    }
}