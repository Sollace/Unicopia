package com.minelittlepony.unicopia.network;

import java.util.function.Function;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public interface Channel {

    SPacketType<MsgPlayerAbility<?>> PLAYER_ABILITY = clientToServer(new Identifier("unicopia", "player_ability"), MsgPlayerAbility::new);

    SPacketType<MsgRequestCapabilities> REQUEST_CAPABILITIES = clientToServer(new Identifier("unicopia", "request_capabilities"), MsgRequestCapabilities::new);
    CPacketType<MsgPlayerCapabilities> PLAYER_CAPABILITIES = serverToClient(new Identifier("unicopia", "player_capabilities"), MsgPlayerCapabilities::new);

    SPacketType<MsgPlayerCapabilities> BROADCAST_CAPABILITIES = broadcast(PLAYER_CAPABILITIES, MsgPlayerCapabilities::new);

    CPacketType<MsgSpawnRainbow> SPAWN_RAINBOW = serverToClient(new Identifier("unicopia", "rainbow_entity"), MsgSpawnRainbow::new);

    static void bootstrap() { }

    static <T extends Packet> SPacketType<T> clientToServer(Identifier id, Function<PacketByteBuf, T> factory) {
        ServerSidePacketRegistry.INSTANCE.register(id, (context, buffer) -> factory.apply(buffer).handle(context));
        return () -> id;
    }

    static <T extends Packet> SPacketType<T> broadcast(CPacketType<T> redirect, Function<PacketByteBuf, T> factory) {
        Identifier id = new Identifier(redirect.getId().getNamespace(), "broadcast_" + redirect.getId().getPath());
        ServerSidePacketRegistry.INSTANCE.register(id, (context, buffer) -> {
            PlayerEntity sender = context.getPlayer();
            T p = factory.apply(buffer);
            p.handle(context);
            sender.world.getPlayers().forEach(player -> {
                if (player != null && player != sender) {
                    redirect.send(player, p);
                }
            });
        });
        return () -> id;
    }

    static <T extends Packet> CPacketType<T> serverToClient(Identifier id, Function<PacketByteBuf, T> factory) {
        ClientSidePacketRegistry.INSTANCE.register(id, (context, buffer) -> factory.apply(buffer).handle(context));
        return () -> id;
    }

    interface CPacketType<T extends Packet> {
        Identifier getId();

        default void send(PlayerEntity recipient, T packet) {
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(recipient, getId(), packet.toBuffer());
        }

        default net.minecraft.network.Packet<?> toPacket(T packet) {
            return ServerSidePacketRegistry.INSTANCE.toPacket(getId(), packet.toBuffer());
        }
    }

    interface SPacketType<T extends Packet> {
        Identifier getId();

        default void send(T packet) {
            ClientSidePacketRegistry.INSTANCE.sendToServer(getId(), packet.toBuffer());
        }

        default net.minecraft.network.Packet<?> toPacket(T packet) {
            return ClientSidePacketRegistry.INSTANCE.toPacket(getId(), packet.toBuffer());
        }
    }

    interface Packet {
        void handle(PacketContext context);

        void toBuffer(PacketByteBuf buffer);

        default PacketByteBuf toBuffer() {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            toBuffer(buf);
            return buf;
        }
    }
}
