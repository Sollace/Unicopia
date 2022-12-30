package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.Race;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public record MsgTribeSelect (Set<Race> availableRaces) implements Packet<PlayerEntity> {
    public MsgTribeSelect(PacketByteBuf buffer) {
        this(new HashSet<>());
        int len = buffer.readInt();
        while (len-- > 0) {
            availableRaces.add(buffer.readRegistryValue(Race.REGISTRY));
        }
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(availableRaces.size());
        availableRaces.forEach(race -> buffer.writeRegistryValue(Race.REGISTRY, race));
    }
}
