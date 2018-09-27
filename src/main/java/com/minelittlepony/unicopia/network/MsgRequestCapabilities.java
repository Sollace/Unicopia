package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.google.gson.annotations.Expose;
import com.minelittlepony.jumpingcastle.api.IChannel;
import com.minelittlepony.jumpingcastle.api.IMessage;
import com.minelittlepony.jumpingcastle.api.IMessageHandler;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.entity.player.EntityPlayer;

@IMessage.Id(0)
public class MsgRequestCapabilities implements IMessage, IMessageHandler<MsgRequestCapabilities> {
    @Expose
    public UUID senderId;

    @Expose
    public Race race;

    public MsgRequestCapabilities(EntityPlayer player, Race preferredRace) {
        senderId = player.getGameProfile().getId();
        race = preferredRace;
    }

    @Override
    public void onPayload(MsgRequestCapabilities message, IChannel channel) {
        System.out.println("[SERVER] Sending capabilities to player id " + senderId);
        IPlayer player = PlayerSpeciesList.instance().getPlayer(senderId);

        if (player.getPlayerSpecies().isDefault()) {
            player.setPlayerSpecies(message.race);
        }

        channel.respond(new MsgPlayerCapabilities(player), senderId);
    }
}
