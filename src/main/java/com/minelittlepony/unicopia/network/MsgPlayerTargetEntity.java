package com.minelittlepony.unicopia.network;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record MsgPlayerTargetEntity(Optional<Integer> entityId) implements Handled<PlayerEntity> {
    public static final PacketCodec<ByteBuf, MsgPlayerTargetEntity> PACKET_CODEC = PacketCodecs.optional(PacketCodecs.INTEGER).xmap(MsgPlayerTargetEntity::new, MsgPlayerTargetEntity::entityId);

    @Override
    public void handle(PlayerEntity sender) {
        entityId.ifPresentOrElse(entityId -> {
            @Nullable
            Entity target = sender.getWorld().getEntityById(entityId);
            Pony.of(sender).setLookedEntity(target);
        }, () -> {
            Pony.of(sender).setLookedEntity(null);
        });

    }
}
