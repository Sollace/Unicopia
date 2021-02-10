package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class MsgOtherPlayerCapabilities extends MsgPlayerCapabilities {

    MsgOtherPlayerCapabilities(PacketByteBuf buffer) {
        super(buffer);

    }

    public MsgOtherPlayerCapabilities(boolean full, Pony player) {
        super(full, player);

    }

    @Override
    protected Pony getRecipient(PlayerEntity sender) {
        return Pony.of(MinecraftClient.getInstance().world.getPlayerByUuid(playerId));
    }
}
