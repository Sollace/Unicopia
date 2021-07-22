package com.minelittlepony.unicopia.util.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * A broadcast packet type. Sent by the server to all surrounding players.
 */
public interface S2CBroadcastPacketType<T extends Packet<PlayerEntity>> {
    Identifier getId();

    default void send(World world, T packet) {
        world.getPlayers().forEach(player -> {
            if (player instanceof ServerPlayerEntity) {
                ServerPlayNetworking.send((ServerPlayerEntity)player, getId(), packet.toBuffer());
            }
        });
    }
}
