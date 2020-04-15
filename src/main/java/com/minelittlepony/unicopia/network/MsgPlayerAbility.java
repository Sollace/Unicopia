package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.minelittlepony.jumpingcastle.api.Channel;
import com.minelittlepony.jumpingcastle.api.Message;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.entity.player.IPlayer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

public class MsgPlayerAbility implements Message, Message.Handler<MsgPlayerAbility> {

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Expose
    private UUID senderId;

    @Expose
    private String powerIdentifier;

    @Expose
    private String abilityJson;

    public MsgPlayerAbility(PlayerEntity player, Ability<?> power, Ability.IData data) {
        senderId = player.getUuid();
        powerIdentifier = power.getKeyName();
        abilityJson = gson.toJson(data, power.getPackageType());
    }

    private <T extends Ability.IData> void apply(Ability<T> power, Channel channel) {
        MinecraftServer server = channel.getServer();
        IPlayer player = SpeciesList.instance().getPlayer(server.getPlayerManager().getPlayer(senderId));
        if (player == null) {
            return;
        }

        T data = gson.fromJson(abilityJson, power.getPackageType());

        power.apply(player, data);
    }

    @Override
    public void onPayload(MsgPlayerAbility message, Channel channel) {
        Abilities.getInstance().getPowerFromName(powerIdentifier).ifPresent(power -> apply(power, channel));
    }
}
