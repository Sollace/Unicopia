package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.minelittlepony.unicopia.client.render.PlayerPoser.AnimationInstance;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Uuids;

/**
 * Sent to the client when a player's animation changes.
 */
public record MsgPlayerAnimationChange (
        UUID playerId,
        AnimationInstance animation,
        int duration
    ) {
    public static final PacketCodec<PacketByteBuf, MsgPlayerAnimationChange> PACKET_CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, MsgPlayerAnimationChange::playerId,
            AnimationInstance.PACKET_CODEC, MsgPlayerAnimationChange::animation,
            PacketCodecs.INTEGER, MsgPlayerAnimationChange::duration,
            MsgPlayerAnimationChange::new
    );

    public MsgPlayerAnimationChange(Pony player, AnimationInstance animation, int duration) {
        this(player.asEntity().getUuid(), animation, duration);
    }
}
