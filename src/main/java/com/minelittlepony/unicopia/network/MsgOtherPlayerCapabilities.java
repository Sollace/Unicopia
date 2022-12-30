package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

/**
 * Sent by the server to update other player's capabilities.
 */
public class MsgOtherPlayerCapabilities extends MsgPlayerCapabilities {

    MsgOtherPlayerCapabilities(PacketByteBuf buffer) {
        super(buffer);
    }

    public MsgOtherPlayerCapabilities(Pony player) {
        super(player);
    }

    @Override
    protected Pony getRecipient(PlayerEntity sender) {
        return Pony.of(MinecraftClient.getInstance().world.getPlayerByUuid(playerId));
    }
}
