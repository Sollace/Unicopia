package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.Race;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.network.PacketByteBuf;

public record MsgTribeSelect (Set<Race> availableRaces, String serverMessage) implements Packet {
    public MsgTribeSelect(PacketByteBuf buffer) {
        this(
            buffer.readCollection(HashSet::new, buf -> buf.readRegistryValue(Race.REGISTRY)),
            buffer.readString()
        );
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeCollection(availableRaces, (buf, race) -> buf.writeRegistryValue(Race.REGISTRY, race));
        buffer.writeString(serverMessage);
    }
}
