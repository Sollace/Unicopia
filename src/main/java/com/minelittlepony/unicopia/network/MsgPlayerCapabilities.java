package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * Sent to the client to update various data pertaining to a particular player.
 * <p>
 * Also used by the server to notify a race change.
 */
public class MsgPlayerCapabilities implements Handled<PlayerEntity> {
    public static final PacketCodec<RegistryByteBuf, MsgPlayerCapabilities> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, i -> i.playerId,
            PacketCodecs.NBT_COMPOUND, i -> i.compoundTag,
            PacketCodecs.BOOLEAN, i -> i.initial,
            MsgPlayerCapabilities::new
    );

    protected final int playerId;

    private final NbtCompound compoundTag;
    private final boolean initial;

    MsgPlayerCapabilities(int playerId, NbtCompound compoundTag, boolean initial) {
        this.playerId = playerId;
        this.compoundTag = compoundTag;
        this.initial = initial;
    }

    public MsgPlayerCapabilities(Pony player, boolean initial) {
        playerId = player.asEntity().getId();
        compoundTag = new NbtCompound();
        this.initial = initial;
        if (initial) {
            player.toNBT(compoundTag, player.asWorld().getRegistryManager());
        } else {
            player.toSyncronisedNbt(compoundTag, player.asWorld().getRegistryManager());
        }
    }

    @Override
    public void handle(PlayerEntity sender) {
        Pony player = Pony.of(sender.getWorld().getEntityById(playerId)).orElse(null);
        if (player != null) {
            if (initial) {
                player.fromNBT(compoundTag, sender.getWorld().getRegistryManager());
            } else {
                player.fromSynchronizedNbt(compoundTag, sender.getWorld().getRegistryManager());
            }
        }
    }
}
