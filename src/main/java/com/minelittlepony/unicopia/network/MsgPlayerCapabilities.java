package com.minelittlepony.unicopia.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Packet;

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
public class MsgPlayerCapabilities implements Packet<PlayerEntity> {

    protected final UUID playerId;

    private final NbtCompound compoundTag;

    MsgPlayerCapabilities(PacketByteBuf buffer) {
        playerId = buffer.readUuid();
        try (InputStream in = new ByteBufInputStream(buffer)) {
            compoundTag = NbtIo.readCompressed(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MsgPlayerCapabilities(Pony player) {
        playerId = player.asEntity().getUuid();
        compoundTag = new NbtCompound();
        player.toSyncronisedNbt(compoundTag);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeUuid(playerId);
        try (OutputStream out = new ByteBufOutputStream(buffer)) {
            NbtIo.writeCompressed(compoundTag, out);
        } catch (IOException e) {
        }
    }

    @Override
    public void handle(PlayerEntity sender) {
        Pony player = getRecipient(sender);
        if (player == null) {
            Unicopia.LOGGER.warn("Skipping capabilities for unknown player " + playerId.toString());
            return;
        }

        player.fromSynchronizedNbt(compoundTag);
    }

    protected Pony getRecipient(PlayerEntity sender) {
        return Pony.of(sender);
    }
}
