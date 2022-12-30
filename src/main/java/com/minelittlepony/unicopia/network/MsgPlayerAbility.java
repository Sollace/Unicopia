package com.minelittlepony.unicopia.network;

import java.util.Optional;

import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.ActivationType;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.HandledPacket;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Sent to the server when a player activates an ability.
 */
public record MsgPlayerAbility<T extends Hit> (
        Ability<T> power,
        Optional<T> data,
        ActivationType type
    ) implements HandledPacket<ServerPlayerEntity> {

    @SuppressWarnings("unchecked")
    static <T extends Hit> MsgPlayerAbility<T> read(PacketByteBuf buffer) {
        Ability<T> power = (Ability<T>) Abilities.REGISTRY.get(buffer.readIdentifier());
        return new MsgPlayerAbility<>(
            power,
            buffer.readOptional(power.getSerializer()::fromBuffer),
            ActivationType.of(buffer.readInt())
        );
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeIdentifier(Abilities.REGISTRY.getId(power));
        buffer.writeOptional(data, (buf, t) -> t.toBuffer(buf));
        buffer.writeInt(type.ordinal());
    }

    @Override
    public void handle(ServerPlayerEntity sender) {
        Pony player = Pony.of(sender);
        if (player == null) {
            return;
        }

        if (type != ActivationType.NONE) {
            power.onQuickAction(player, type, data);
        } else {
            data.filter(data -> power.canApply(player, data)).ifPresentOrElse(
                    data -> power.apply(player, data),
                    () -> Channel.CANCEL_PLAYER_ABILITY.sendToPlayer(new MsgCancelPlayerAbility(), sender)
            );
        }
    }
}
