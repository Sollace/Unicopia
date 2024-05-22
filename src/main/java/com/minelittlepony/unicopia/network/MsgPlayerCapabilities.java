package com.minelittlepony.unicopia.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.HandledPacket;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

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
        try (InputStream in = new ByteBufInputStream(buffer)) {
            compoundTag = NbtIo.readCompressed(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MsgPlayerCapabilities(Pony player) {
        playerId = player.asEntity().getId();
        compoundTag = new NbtCompound();
        player.toSyncronisedNbt(compoundTag);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(playerId);
        try (OutputStream out = new ByteBufOutputStream(buffer)) {
            NbtIo.writeCompressed(compoundTag, out);
        } catch (IOException e) {
        }
    }

    @Override
    public void handle(PlayerEntity sender) {
        Pony player = Pony.of(sender.getWorld().getEntityById(playerId)).orElse(null);
        if (player != null) {
            player.fromSynchronizedNbt(compoundTag);
        }
    }
}
