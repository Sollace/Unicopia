package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.Race;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record MsgTribeSelect (Set<Race> availableRaces, String serverMessage) {
    public static final PacketCodec<RegistryByteBuf, MsgTribeSelect> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryValue(Race.REGISTRY_KEY).collect(PacketCodecs.toCollection(HashSet::new)), MsgTribeSelect::availableRaces,
            PacketCodecs.STRING, MsgTribeSelect::serverMessage,
            MsgTribeSelect::new
    );
}
