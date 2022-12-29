package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.InteractionManager;
import com.sollace.fabwork.api.packets.Packet;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Sent by the server to update block destruction progress on the client.
 */
public class MsgBlockDestruction implements Packet<PlayerEntity> {

    private final Long2ObjectMap<Float> destructions;

    MsgBlockDestruction(PacketByteBuf buffer) {
        destructions = new Long2ObjectOpenHashMap<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            destructions.put(buffer.readLong(), (Float)buffer.readFloat());
        }
    }

    public MsgBlockDestruction(Long2ObjectMap<Float> destructions) {
        this.destructions = destructions;
    }

    public Long2ObjectMap<Float> getDestructions() {
        return destructions;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(destructions.size());
        destructions.forEach((p, i) -> {
            buffer.writeLong(p);
            buffer.writeFloat(i);
        });
    }

    @Override
    public void handle(PlayerEntity sender) {
        InteractionManager.instance().getClientNetworkHandler().handleBlockDestruction(this);
    }
}
