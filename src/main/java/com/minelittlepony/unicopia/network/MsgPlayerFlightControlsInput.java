package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.HandledPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public record MsgPlayerFlightControlsInput (
        boolean ascending,
        boolean descending
    ) implements HandledPacket<ServerPlayerEntity> {

    public MsgPlayerFlightControlsInput(Pony pony) {
        this(pony.getJumpingHeuristic().getState(), pony.asEntity().isSneaking());
    }

    public MsgPlayerFlightControlsInput(PacketByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeBoolean(ascending);
        buffer.writeBoolean(descending);
    }

    @Override
    public void handle(ServerPlayerEntity sender) {
        sender.setSneaking(descending);
        sender.setJumping(ascending);
    }
}
