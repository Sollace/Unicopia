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
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

@IMessage.Id(1)
public class MsgPlayerCapabilities implements IMessage, IMessageHandler<MsgPlayerCapabilities> {
    @Expose
    public Race newRace;

    @Expose
    UUID senderId;

    @Expose
    byte[] compoundTag;

    public MsgPlayerCapabilities(Race race, EntityPlayer player) {
        this(race, player.getGameProfile().getId());
    }

    public MsgPlayerCapabilities(IPlayer player) {
        newRace = player.getPlayerSpecies();
        senderId = player.getOwner().getGameProfile().getId();

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            NBTTagCompound nbt = player.toNBT();

            CompressedStreamTools.write(nbt, new DataOutputStream(bytes));

            compoundTag = bytes.toByteArray();
        } catch (IOException e) {
        }
    }

    public MsgPlayerCapabilities(Race race, UUID playerId) {
        newRace = race;
        senderId = playerId;
        compoundTag = new byte[0];
    }

    @Override
    public void onPayload(MsgPlayerCapabilities message, IChannel channel) {
        EntityPlayer self = Minecraft.getMinecraft().player;
        UUID myid = self.getGameProfile().getId();

        IPlayer player;
        if (senderId.equals(myid)) {
            player = PlayerSpeciesList.instance().getPlayer(self);
        } else {
            EntityPlayer found = Minecraft.getMinecraft().world.getPlayerEntityByUUID(senderId);

            if (found == null) {
                System.out.println("Player with id " + senderId + " was not found!");
                return;
            }

            player = PlayerSpeciesList.instance().getPlayer(found);
        }

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
