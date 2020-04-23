package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.PacketByteBuf;

public class MsgRequestCapabilities implements Channel.Packet {

    private final Race race;

    public MsgRequestCapabilities(PacketByteBuf buffer) {
        race = Race.values()[buffer.readInt()];
    }

    public MsgRequestCapabilities(PlayerEntity player, Race preferredRace) {
        race = preferredRace;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(race.ordinal());
    }

    @Override
    public void handle(PacketContext context) {
        Pony player = Pony.of(context.getPlayer());

        if (player.getSpecies().isDefault()) {
            player.setSpecies(race);
        }

        Channel.PLAYER_CAPABILITIES.send(context.getPlayer(), new MsgPlayerCapabilities(true, player));
    }

}
