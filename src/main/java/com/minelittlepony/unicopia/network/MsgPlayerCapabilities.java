package com.minelittlepony.unicopia.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import com.google.gson.annotations.Expose;
import com.minelittlepony.jumpingcastle.api.IChannel;
import com.minelittlepony.jumpingcastle.api.IMessage;
import com.minelittlepony.jumpingcastle.api.IMessageHandler;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.capabilities.IPlayer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundTag;

@IMessage.Id(1)
public class MsgPlayerCapabilities implements IMessage, IMessageHandler<MsgPlayerCapabilities> {
    @Expose
    Race newRace;

    @Expose
    UUID senderId;

    @Expose
    byte[] compoundTag;

    public MsgPlayerCapabilities(Race race, PlayerEntity player) {
        newRace = race;
        senderId = player.getUniqueID();
        compoundTag = new byte[0];
    }

    public MsgPlayerCapabilities(IPlayer player) {
        newRace = player.getSpecies();
        senderId = player.getOwner().getUniqueID();

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            CompoundTag nbt = player.toNBT();

            CompressedStreamTools.write(nbt, new DataOutputStream(bytes));

            compoundTag = bytes.toByteArray();
        } catch (IOException e) {
        }
    }

    @Override
    public void onPayload(MsgPlayerCapabilities message, IChannel channel) {
        PlayerEntity self = UClient.instance().getPlayerByUUID(senderId);

        if (self == null) {
            Unicopia.LOGGER.warn("[Unicopia] [CLIENT] [MsgPlayerCapabilities] Player with id %s was not found!\n", senderId.toString());
        } else {
            IPlayer player = SpeciesList.instance().getPlayer(self);

            if (compoundTag.length > 0) {
                try (ByteArrayInputStream input = new ByteArrayInputStream(compoundTag)) {
                    CompoundTag nbt = CompressedStreamTools.read(new DataInputStream(input));

                    player.fromNBT(nbt);
                } catch (IOException e) {

                }
            } else {
                player.setSpecies(newRace);
            }
        }
    }
}
