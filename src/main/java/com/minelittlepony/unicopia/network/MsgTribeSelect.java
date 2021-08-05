package com.minelittlepony.unicopia.network;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.client.gui.TribeSelectionScreen;
import com.minelittlepony.unicopia.util.network.Packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class MsgTribeSelect implements Packet<PlayerEntity> {
    private final Set<Race> availableRaces;

    public MsgTribeSelect(PlayerEntity player) {
        availableRaces = Race.all().stream().filter(r -> r.isPermitted(player)).collect(Collectors.toSet());
    }

    public MsgTribeSelect(PacketByteBuf buffer) {
        int len = buffer.readInt();
        availableRaces = new HashSet<>();
        while (len-- > 0) {
            availableRaces.add(Race.fromId(buffer.readInt()));
        }
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(availableRaces.size());
        availableRaces.forEach(race -> buffer.writeInt(race.ordinal()));
    }

    @Override
    public void handle(PlayerEntity sender) {
        MinecraftClient.getInstance().openScreen(new TribeSelectionScreen(availableRaces));
    }
}
