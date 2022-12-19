package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.network.Packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

/**
 * Sent to the client when a player's animation changes.
 */
public class MsgPlayerAnimationChange implements Packet<PlayerEntity> {
    private final UUID playerId;
    private final Animation animation;
    private final int duration;

    MsgPlayerAnimationChange(PacketByteBuf buffer) {
        playerId = buffer.readUuid();
        animation = Animation.values()[buffer.readInt()];
        duration = buffer.readInt();
    }

    public MsgPlayerAnimationChange(Pony player, Animation animation, int duration) {
        this.playerId = player.asEntity().getUuid();
        this.animation = animation;
        this.duration = duration;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeUuid(playerId);
        buffer.writeInt(animation.ordinal());
        buffer.writeInt(duration);
    }

    @Override
    public void handle(PlayerEntity sender) {
        Pony player = Pony.of(MinecraftClient.getInstance().world.getPlayerByUuid(playerId));
        if (player == null) {
            return;
        }

        player.setAnimation(animation, duration);
    }
}
