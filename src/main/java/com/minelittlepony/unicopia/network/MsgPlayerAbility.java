package com.minelittlepony.unicopia.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.PacketByteBuf;

public class MsgPlayerAbility implements Channel.Packet {

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    private final String powerIdentifier;

    private final String abilityJson;

    public MsgPlayerAbility(Ability<?> power, Ability.IData data) {
        powerIdentifier = power.getKeyName();
        abilityJson = gson.toJson(data, power.getPackageType());
    }

    public MsgPlayerAbility(PacketByteBuf buffer) {
        powerIdentifier = buffer.readString();
        abilityJson = buffer.readString();
    }

    private <T extends Ability.IData> void apply(Ability<T> power, PacketContext context) {
        Pony player = Pony.of(context.getPlayer());
        if (player == null) {
            return;
        }

        T data = gson.fromJson(abilityJson, power.getPackageType());

        power.apply(player, data);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeString(powerIdentifier);
        buffer.writeString(abilityJson);
    }

    @Override
    public void handle(PacketContext context) {
        Abilities.getInstance().getPowerFromName(powerIdentifier).ifPresent(power -> apply(power, context));
    }
}
