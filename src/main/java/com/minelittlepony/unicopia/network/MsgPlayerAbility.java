package com.minelittlepony.unicopia.network;

import java.util.Optional;
import com.minelittlepony.unicopia.ability.Abilities;
import com.minelittlepony.unicopia.ability.Ability;
import com.minelittlepony.unicopia.ability.ActivationType;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Sent to the server when a player activates an ability.
 */
public record MsgPlayerAbility<T extends Hit> (
        Ability<T> power,
        Optional<T> data,
        ActivationType type
    ) implements Handled<ServerPlayerEntity> {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final PacketCodec<RegistryByteBuf, MsgPlayerAbility<?>> PACKET_CODEC = PacketCodec.of(
            (packet, buffer) -> {
                Abilities.PACKET_CODEC.encode(buffer, packet.power());
                buffer.writeOptional(packet.data(), (b, d) -> ((PacketCodec)packet.power().getSerializer()).encode(buffer, d));
                ActivationType.PACKET_CODEC.encode(buffer, packet.type());
            },
            buffer -> {
                Ability<?> power = Abilities.PACKET_CODEC.decode(buffer);
                return new MsgPlayerAbility(
                    power,
                    buffer.readOptional(b -> power.getSerializer().decode(buffer)),
                    ActivationType.PACKET_CODEC.decode(buffer)
                );
            }
    );

    @Override
    public void handle(ServerPlayerEntity sender) {
        Pony player = Pony.of(sender);

        if (player == null) {
            return;
        }

        if (type != ActivationType.NONE) {
            power.onQuickAction(player, type, data);
        } else {
            if (data.filter(data -> power.apply(player, data)).isEmpty()) {
                Channel.CANCEL_PLAYER_ABILITY.sendToPlayer(new MsgCancelPlayerAbility(), sender);
            }
        }
    }
}
