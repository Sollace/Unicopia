package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.google.gson.annotations.Expose;
import com.minelittlepony.jumpingcastle.api.IMessage;
import com.minelittlepony.unicopia.Race;

import net.minecraft.entity.player.EntityPlayer;

@IMessage.Id(1)
public class MsgPlayerCapabilities implements IMessage {
    @Expose
    public Race newRace;

    @Expose
    public UUID senderId;

    public MsgPlayerCapabilities(Race race, EntityPlayer player) {
        newRace = race;
        senderId = player.getGameProfile().getId();
    }

    public MsgPlayerCapabilities(Race race, UUID playerId) {
        newRace = race;
        senderId = playerId;
    }
}
