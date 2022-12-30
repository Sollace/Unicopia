package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public record MsgMarkTraitRead (Set<Trait> traits) implements Packet<ServerPlayerEntity> {
    MsgMarkTraitRead(PacketByteBuf buffer) {
        this(new HashSet<>());
        int length = buffer.readInt();
        for (int i = 0; i < length; i++) {
            Trait.fromId(buffer.readIdentifier()).ifPresent(traits::add);
        }
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(traits.size());
        traits.forEach(trait -> buffer.writeIdentifier(trait.getId()));
    }

    @Override
    public void handle(ServerPlayerEntity sender) {
        Pony.of(sender).getDiscoveries().markRead(traits);
    }
}
