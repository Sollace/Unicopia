package com.minelittlepony.unicopia.network;

import com.sollace.fabwork.api.packets.Packet;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Sent by the server to update block destruction progress on the client.
 */
public record MsgBlockDestruction (Long2ObjectMap<Float> destructions) implements Packet<PlayerEntity> {
    MsgBlockDestruction(PacketByteBuf buffer) {
        this(new Long2ObjectOpenHashMap<>());
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            destructions.put(buffer.readLong(), (Float)buffer.readFloat());
        }
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(destructions.size());
        destructions.forEach((p, i) -> {
            buffer.writeLong(p);
            buffer.writeFloat(i);
        });
    }
}
