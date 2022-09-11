package com.minelittlepony.unicopia.util.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A client packet type. Sent by the server to a specific player.
 */
public interface S2CPacketType<T extends Packet<? extends PlayerEntity>> {
    Identifier getId();

    default void send(ServerPlayerEntity recipient, T packet) {
        ServerPlayNetworking.send(recipient, getId(), packet.toBuffer());
    }

    default net.minecraft.network.Packet<?> toPacket(T packet) {
        return ServerPlayNetworking.createS2CPacket(getId(), packet.toBuffer());
    }
}