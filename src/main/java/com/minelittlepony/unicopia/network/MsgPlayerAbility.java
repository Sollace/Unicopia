package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.ability.Abilities;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;

public class MsgPlayerAbility<T extends Hit> implements Channel.Packet {

    private final Ability<T> power;

    private final T data;

    @SuppressWarnings("unchecked")
    MsgPlayerAbility(PacketByteBuf buffer) {
        power = (Ability<T>) Abilities.REGISTRY.get(new Identifier(buffer.readString()));
        data = power.getSerializer().fromBuffer(buffer);
    }

    public MsgPlayerAbility(Ability<T> power, T data) {
        this.power = power;
        this.data = data;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeString(Abilities.REGISTRY.getId(power).toString());
        data.toBuffer(buffer);
    }

    @Override
    public void handle(PacketContext context) {
        Pony player = Pony.of(context.getPlayer());
        if (player == null) {
            return;
        }

        power.apply(player, data);
    }
}
