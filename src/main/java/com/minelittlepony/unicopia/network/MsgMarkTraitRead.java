package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;

public record MsgMarkTraitRead (Set<Trait> traits) implements Handled<ServerPlayerEntity> {
    public static final PacketCodec<PacketByteBuf, MsgMarkTraitRead> PACKET_CODEC = PacketCodec.tuple(
            Trait.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), MsgMarkTraitRead::traits,
            MsgMarkTraitRead::new
    );

    @Override
    public void handle(ServerPlayerEntity sender) {
        Pony.of(sender).getDiscoveries().markRead(traits);
    }
}
