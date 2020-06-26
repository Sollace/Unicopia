package com.minelittlepony.unicopia.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.equine.player.Pony;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

public class MsgPlayerCapabilities implements Channel.Packet {

    private final Race newRace;

    private final CompoundTag compoundTag;

    MsgPlayerCapabilities(PacketByteBuf buffer) {
        newRace = Race.values()[buffer.readInt()];
        try (InputStream in = new ByteBufInputStream(buffer)) {
            compoundTag = NbtIo.readCompressed(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MsgPlayerCapabilities(Race race, PlayerEntity player) {
        newRace = race;
        compoundTag = new CompoundTag();
    }

    public MsgPlayerCapabilities(boolean full, Pony player) {
        newRace = player.getSpecies();
        compoundTag = full ? player.toNBT() : new CompoundTag();
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(newRace.ordinal());
        try (OutputStream out = new ByteBufOutputStream(buffer)) {
            NbtIo.writeCompressed(compoundTag, out);
        } catch (IOException e) {
        }
    }

    @Override
    public void handle(PacketContext context) {
        System.out.println("Got capabilities for player " + newRace + " " + context.getPacketEnvironment());
        Pony player = Pony.of(context.getPlayer());
        if (compoundTag.isEmpty()) {
            player.setSpecies(newRace);
        } else {
            player.fromNBT(compoundTag);
        }
    }
}
