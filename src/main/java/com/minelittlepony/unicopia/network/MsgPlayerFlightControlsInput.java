package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;

public record MsgPlayerFlightControlsInput (
        boolean ascending,
        boolean descending
    ) implements Handled<ServerPlayerEntity> {
    public static final PacketCodec<ByteBuf, MsgPlayerFlightControlsInput> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, MsgPlayerFlightControlsInput::ascending,
            PacketCodecs.BOOL, MsgPlayerFlightControlsInput::descending,
            MsgPlayerFlightControlsInput::new
    );

    public MsgPlayerFlightControlsInput(Pony pony) {
        this(pony.getJumpingHeuristic().getState(), pony.asEntity().isSneaking());
    }

    @Override
    public void handle(ServerPlayerEntity sender) {
        sender.setSneaking(descending);
        sender.setJumping(ascending);
    }
}
