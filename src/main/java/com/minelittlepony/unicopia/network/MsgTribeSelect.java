package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.Race;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public record MsgTribeSelect (Set<Race> availableRaces, Text serverMessage) implements Packet<PlayerEntity> {
    public MsgTribeSelect(PacketByteBuf buffer) {
        this(
            buffer.readCollection(HashSet::new, buf -> buf.readRegistryValue(Race.REGISTRY)),
            buffer.readText()
        );
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeCollection(availableRaces, (buf, race) -> buf.writeRegistryValue(Race.REGISTRY, race));
        buffer.writeText(serverMessage);
    }
}
