package com.minelittlepony.unicopia.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.network.Packet;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

public class MsgPlayerCapabilities implements Packet<PlayerEntity> {

    protected final UUID playerId;

    private final Race newRace;

    private final NbtCompound compoundTag;

    MsgPlayerCapabilities(PacketByteBuf buffer) {
        playerId = buffer.readUuid();
        newRace = Race.values()[buffer.readInt()];
        try (InputStream in = new ByteBufInputStream(buffer)) {
            compoundTag = NbtIo.readCompressed(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MsgPlayerCapabilities(boolean full, Pony player) {
        playerId = player.getMaster().getUuid();
        newRace = player.getSpecies();
        compoundTag = full ? player.toNBT() : new NbtCompound();
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeUuid(playerId);
        buffer.writeInt(newRace.ordinal());
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
        if (compoundTag.isEmpty()) {
            player.setSpecies(newRace);
        } else {
            player.fromNBT(compoundTag);
        }
    }

    protected Pony getRecipient(PlayerEntity sender) {
        return Pony.of(sender);
    }
}
