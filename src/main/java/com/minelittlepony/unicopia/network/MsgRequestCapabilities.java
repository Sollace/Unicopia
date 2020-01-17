package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.google.gson.annotations.Expose;
import com.minelittlepony.jumpingcastle.api.IChannel;
import com.minelittlepony.jumpingcastle.api.IMessage;
import com.minelittlepony.jumpingcastle.api.IMessageHandler;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.capabilities.IPlayer;

import net.minecraft.entity.player.PlayerEntity;

@IMessage.Id(0)
public class MsgRequestCapabilities implements IMessage, IMessageHandler<MsgRequestCapabilities> {
    @Expose
    public UUID senderId;

    @Expose
    public Race race;

    public MsgRequestCapabilities(PlayerEntity player, Race preferredRace) {
        senderId = player.getGameProfile().getId();
        race = preferredRace;
    }

    @Override
    public void onPayload(MsgRequestCapabilities message, IChannel channel) {
        Unicopia.LOGGER.warn("[Unicopia] [SERVER] [MsgRequestCapabilities] Sending capabilities to player %s\n", senderId.toString());
        IPlayer player = SpeciesList.instance().getPlayer(senderId);

        if (player.getSpecies().isDefault()) {
            player.setSpecies(message.race);
        }

        channel.respond(new MsgPlayerCapabilities(player), senderId);
    }
}
