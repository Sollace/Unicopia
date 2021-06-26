package com.minelittlepony.unicopia.util.network;

import com.google.common.base.Preconditions;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A server packet type. Sent by the client to the server.
 */
public interface S2CPacketType<T extends Packet<ServerPlayerEntity>> {
    Identifier getId();

    default void send(T packet) {
        Preconditions.checkState(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT, "Client packet send called by the server");
        ClientPlayNetworking.send(getId(), packet.toBuffer());
    }
}