package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.server.world.UGameRules;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.sollace.fabwork.api.packets.HandledPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;

/**
 * Sent to the server when a client wants to request a species change.
 * <p>
 * The server responds back with the accepted capabilities and the race the client should use (if the preferred was not permitted)
 */
public record MsgRequestSpeciesChange (
        boolean force,
        Race newRace
    ) implements HandledPacket<ServerPlayerEntity> {

    MsgRequestSpeciesChange(PacketByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readRegistryValue(Race.REGISTRY));
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeBoolean(force);
        buffer.writeRegistryValue(Race.REGISTRY, newRace);
    }

    @Override
    public void handle(ServerPlayerEntity sender) {
        Pony player = Pony.of(sender);

        if (force || player.getSpecies().isUnset()) {
            boolean permitted = newRace.isPermitted(sender);
            player.setSpecies(permitted ? newRace : UnicopiaWorldProperties.forWorld((ServerWorld)player.asWorld()).getDefaultRace());
            if (!permitted) {
                sender.sendMessageToClient(Text.translatable("respawn.reason.illegal_race", newRace.getDisplayName()), false);
            }

            if (force) {
                if (sender.getWorld().getGameRules().getBoolean(UGameRules.ANNOUNCE_TRIBE_JOINS)) {
                    Text message = Text.translatable("respawn.reason.joined_new_tribe",
                            sender.getDisplayName(),
                            player.getSpecies().getDisplayName(), player.getSpecies().getAltDisplayName());
                    sender.getWorld().getPlayers().forEach(p -> {
                        ((ServerPlayerEntity)p).sendMessageToClient(message, false);
                    });
                }

                player.onSpawn();
            }
        }

        Channel.SERVER_PLAYER_CAPABILITIES.sendToPlayer(new MsgPlayerCapabilities(player), sender);
    }
}
