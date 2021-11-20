package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.network.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class MsgMarkTraitRead implements Packet<ServerPlayerEntity> {

    public final Set<Trait> traits = new HashSet<>();

    MsgMarkTraitRead(PacketByteBuf buffer) {
        int length = buffer.readInt();
        for (int i = 0; i < length; i++) {
            Trait.fromId(buffer.readIdentifier()).ifPresent(traits::add);
        }
    }

    public MsgMarkTraitRead(Set<Trait> traits) {
        this.traits.addAll(traits);
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
