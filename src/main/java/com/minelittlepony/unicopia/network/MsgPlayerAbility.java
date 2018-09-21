package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.minelittlepony.jumpingcastle.api.IChannel;
import com.minelittlepony.jumpingcastle.api.IMessage;
import com.minelittlepony.jumpingcastle.api.IMessageHandler;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.power.IData;
import com.minelittlepony.unicopia.power.IPower;
import com.minelittlepony.unicopia.power.PowersRegistry;

import net.minecraft.entity.player.EntityPlayer;

@IMessage.Id(2)
public class MsgPlayerAbility implements IMessage, IMessageHandler<MsgPlayerAbility> {

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Expose
    private UUID senderId;

    @Expose
    private String powerIdentifier;

    @Expose
    private String abilityJson;

    public MsgPlayerAbility(EntityPlayer player, IPower<?> power, IData data) {
        senderId = player.getUniqueID();
        powerIdentifier = power.getKeyName();
        abilityJson = gson.toJson(data, power.getPackageType());
    }

    private <T extends IData> void apply(IPower<T> power) {
        EntityPlayer player = IPlayer.getPlayerFromServer(senderId);
        if (player == null) {
            return;
        }

        T data = gson.fromJson(abilityJson, power.getPackageType());

        power.apply(player, data);
    }

    @Override
    public void onPayload(MsgPlayerAbility message, IChannel channel) {
        PowersRegistry.instance().getPowerFromName(powerIdentifier).ifPresent(this::apply);
    }
}
