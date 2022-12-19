package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.WorldTribeManager;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.network.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Sent to the server when a client wants to request a species change.
 * <p>
 * The server responds back with the accepted capabilities and the race the client should use (if the preferred was not permitted)
 */
public class MsgRequestSpeciesChange implements Packet<ServerPlayerEntity> {

    private final boolean force;
    private final Race newRace;

    MsgRequestSpeciesChange(PacketByteBuf buffer) {
        force = buffer.readBoolean();
        newRace = buffer.readRegistryValue(Race.REGISTRY);
    }

    public MsgRequestSpeciesChange(Race newRace) {
        this(newRace, false);
    }

    public MsgRequestSpeciesChange(Race newRace, boolean force) {
        this.newRace = newRace;
        this.force = force;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeBoolean(force);
        buffer.writeRegistryValue(Race.REGISTRY, newRace);
    }

    @Override
    public void handle(ServerPlayerEntity sender) {
        Pony player = Pony.of(sender);

        Race worldDefaultRace = WorldTribeManager.forWorld((ServerWorld)player.asWorld()).getDefaultRace();

        if (force || player.getActualSpecies().isDefault() || (player.getActualSpecies() == worldDefaultRace && !player.isSpeciesPersisted())) {
            player.setSpecies(newRace.isPermitted(sender) ? newRace : worldDefaultRace);

            if (force) {
                player.onSpawn();
            }
        }

        Channel.SERVER_PLAYER_CAPABILITIES.send(sender, new MsgPlayerCapabilities(player));
    }
}
