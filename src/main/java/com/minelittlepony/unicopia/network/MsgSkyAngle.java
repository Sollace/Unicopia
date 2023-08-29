package com.minelittlepony.unicopia.network;

import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public record MsgSkyAngle (
        float tangentalSkyAngle
    ) implements Packet<PlayerEntity> {

    public MsgSkyAngle(PacketByteBuf buffer) {
        this(buffer.readFloat());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeFloat(tangentalSkyAngle());
    }
}
