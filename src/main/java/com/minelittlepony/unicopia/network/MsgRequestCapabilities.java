package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.google.gson.annotations.Expose;
import com.minelittlepony.jumpingcastle.api.Channel;
import com.minelittlepony.jumpingcastle.api.Message;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UnicopiaCore;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

public class MsgRequestCapabilities implements Message, Message.Handler<MsgRequestCapabilities> {
    @Expose
    public UUID senderId;

    @Expose
    public Race race;

    public MsgRequestCapabilities(PlayerEntity player, Race preferredRace) {
        senderId = player.getGameProfile().getId();
        race = preferredRace;
    }

    @Override
    public void onPayload(MsgRequestCapabilities message, Channel channel) {
        MinecraftServer server = channel.getServer();

        UnicopiaCore.LOGGER.warn("[Unicopia] [SERVER] [MsgRequestCapabilities] Sending capabilities to player %s\n", senderId.toString());
        Pony player = Pony.of(server.getPlayerManager().getPlayer(senderId));

        if (player.getSpecies().isDefault()) {
            player.setSpecies(message.race);
        }

        channel.respond(new MsgPlayerCapabilities(player), senderId);
    }
}
