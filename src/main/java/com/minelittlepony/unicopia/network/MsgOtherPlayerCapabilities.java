package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

public class MsgOtherPlayerCapabilities extends MsgPlayerCapabilities {

    MsgOtherPlayerCapabilities(PacketByteBuf buffer) {
        super(buffer);

    }

    public MsgOtherPlayerCapabilities(boolean full, Pony player) {
        super(full, player);

    }

    @Override
    protected Pony getRecipient(PacketContext context) {
        return Pony.of(MinecraftClient.getInstance().world.getPlayerByUuid(playerId));
    }
}
