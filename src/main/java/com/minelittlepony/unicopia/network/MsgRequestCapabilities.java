package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.WorldTribeManager;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;

public class MsgRequestCapabilities implements Channel.Packet {

    private final Race clientPreferredRace;

    MsgRequestCapabilities(PacketByteBuf buffer) {
        clientPreferredRace = Race.values()[buffer.readInt()];
    }

    public MsgRequestCapabilities(Race preferredRace) {
        clientPreferredRace = preferredRace;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(clientPreferredRace.ordinal());
    }

    @Override
    public void handle(PacketContext context) {
        Pony player = Pony.of(context.getPlayer());

        Race worldDefaultRace = WorldTribeManager.forWorld((ServerWorld)player.getWorld()).getDefaultRace();

        if (player.getSpecies().isDefault() || (player.getSpecies() == worldDefaultRace && !player.isSpeciesPersisted())) {
            player.setSpecies(clientPreferredRace.isPermitted(context.getPlayer()) ? clientPreferredRace : worldDefaultRace);
        }

        Channel.SERVER_PLAYER_CAPABILITIES.send(context.getPlayer(), new MsgPlayerCapabilities(true, player));
    }
}
