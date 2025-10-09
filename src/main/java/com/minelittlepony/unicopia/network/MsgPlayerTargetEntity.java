package com.minelittlepony.unicopia.network;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.sollace.fabwork.api.packets.Handled;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;

public record MsgPlayerTargetEntity(Optional<Integer> entityId, Optional<LevitatingItemEntity.Action> action, Optional<Vec3d> direction) implements Handled<PlayerEntity> {
    public static final PacketCodec<PacketByteBuf, MsgPlayerTargetEntity> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(PacketCodecs.INTEGER), MsgPlayerTargetEntity::entityId,
            PacketCodecs.optional(PacketCodecUtils.ofEnum(LevitatingItemEntity.Action.class)), MsgPlayerTargetEntity::action,
            PacketCodecs.optional(PacketCodecUtils.VECTOR), MsgPlayerTargetEntity::direction,
            MsgPlayerTargetEntity::new
    );

    @Override
    public void handle(PlayerEntity sender) {
        entityId.ifPresentOrElse(entityId -> {
            @Nullable
            Entity target = sender.getWorld().getEntityById(entityId);
            Pony.of(sender).setLookedEntity(target);
            if (target instanceof LevitatingItemEntity i) {
                action.ifPresent(action -> {
                    i.handleAction(action, sender, direction);
                });
            }
        }, () -> {
            Pony.of(sender).setLookedEntity(null);
        });

    }
}
