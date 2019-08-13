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
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

@IMessage.Id(1)
public class MsgPlayerCapabilities implements IMessage, IMessageHandler<MsgPlayerCapabilities> {
    @Expose
    Race newRace;

    @Expose
    UUID senderId;

    @Expose
    byte[] compoundTag;

    public MsgPlayerCapabilities(Race race, EntityPlayer player) {
        newRace = race;
        senderId = player.getUniqueID();
        compoundTag = new byte[0];
    }

    public MsgPlayerCapabilities(IPlayer player) {
        newRace = player.getPlayerSpecies();
        senderId = player.getOwner().getUniqueID();

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            NBTTagCompound nbt = player.toNBT();

            CompressedStreamTools.write(nbt, new DataOutputStream(bytes));

            compoundTag = bytes.toByteArray();
        } catch (IOException e) {
        }
    }

    @Override
    public void onPayload(MsgPlayerCapabilities message, IChannel channel) {
        EntityPlayer self = UClient.instance().getPlayerByUUID(senderId);

        if (self == null) {
            Unicopia.log.warn("[Unicopia] [CLIENT] [MsgPlayerCapabilities] Player with id %s was not found!\n", senderId.toString());
        } else {
            IPlayer player = PlayerSpeciesList.instance().getPlayer(self);

            if (compoundTag.length > 0) {
                try (ByteArrayInputStream input = new ByteArrayInputStream(compoundTag)) {
                    NBTTagCompound nbt = CompressedStreamTools.read(new DataInputStream(input));

                    player.readFromNBT(nbt);
                } catch (IOException e) {

                }
            } else {
                player.setPlayerSpecies(newRace);
            }
        }
    }
}
