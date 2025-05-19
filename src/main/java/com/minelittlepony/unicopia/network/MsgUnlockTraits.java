package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record MsgUnlockTraits (Set<Trait> traits) {
    public static final PacketCodec<PacketByteBuf, MsgUnlockTraits> PACKET_CODEC = PacketCodec.tuple(
            Trait.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), MsgUnlockTraits::traits,
            MsgUnlockTraits::new
    );

    public MsgUnlockTraits(Set<Trait> traits) {
        this.traits = new HashSet<>(traits);
    }
}
