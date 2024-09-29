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
            MsgPlayerCapabilities::new
    );

    protected final int playerId;

    private final NbtCompound compoundTag;

    MsgPlayerCapabilities(int playerId, NbtCompound compoundTag) {
        this.playerId = playerId;
        this.compoundTag = compoundTag;
    }

    public MsgPlayerCapabilities(Pony player) {
        playerId = player.asEntity().getId();
        compoundTag = new NbtCompound();
        player.toSyncronisedNbt(compoundTag, player.asWorld().getRegistryManager());
    }

    @Override
    public void handle(PlayerEntity sender) {
        Pony player = Pony.of(sender.getWorld().getEntityById(playerId)).orElse(null);
        if (player != null) {
            player.fromSynchronizedNbt(compoundTag, sender.getWorld().getRegistryManager());
        }
    }
}
