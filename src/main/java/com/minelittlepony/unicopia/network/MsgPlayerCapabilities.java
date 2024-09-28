package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.HandledPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

/**
 * Sent to the client to update various data pertaining to a particular player.
 * <p>
 * Also used by the server to notify a race change.
 */
public class MsgPlayerCapabilities implements HandledPacket<PlayerEntity> {

    protected final int playerId;

    private final NbtCompound compoundTag;

    MsgPlayerCapabilities(PacketByteBuf buffer) {
        playerId = buffer.readInt();
        compoundTag = PacketCodecs.NBT_COMPOUND.decode(buffer);
    }

    public MsgPlayerCapabilities(Pony player) {
        playerId = player.asEntity().getId();
        compoundTag = new NbtCompound();
        player.toSyncronisedNbt(compoundTag, player.asWorld().getRegistryManager());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(playerId);
        PacketCodecs.NBT_COMPOUND.encode(buffer, compoundTag);
    }

    @Override
    public void handle(PlayerEntity sender) {
        Pony player = Pony.of(sender.getWorld().getEntityById(playerId)).orElse(null);
        if (player != null) {
            player.fromSynchronizedNbt(compoundTag, sender.getWorld().getRegistryManager());
        }
    }
}
