package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.WorldTribeManager;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.network.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class MsgRequestCapabilities implements Packet<ServerPlayerEntity> {

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
    public void handle(ServerPlayerEntity sender) {
        Pony player = Pony.of(sender);

        Race worldDefaultRace = WorldTribeManager.forWorld((ServerWorld)player.getWorld()).getDefaultRace();

        if (player.getSpecies().isDefault() || (player.getSpecies() == worldDefaultRace && !player.isSpeciesPersisted())) {
            player.setSpecies(clientPreferredRace.isPermitted(sender) ? clientPreferredRace : worldDefaultRace);
        }

        Channel.SERVER_PLAYER_CAPABILITIES.send(sender, new MsgPlayerCapabilities(true, player));
    }
}
