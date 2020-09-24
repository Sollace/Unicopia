package com.minelittlepony.unicopia.network;

import java.util.function.Function;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.network.PacketByteBuf;

public interface Channel {

    SPacketType<MsgPlayerAbility<?>> CLIENT_PLAYER_ABILITY = clientToServer(new Identifier("unicopia", "player_ability"), MsgPlayerAbility::new);
    SPacketType<MsgRequestCapabilities> CLIENT_REQUEST_CAPABILITIES = clientToServer(new Identifier("unicopia", "request_capabilities"), MsgRequestCapabilities::new);
    CPacketType<MsgPlayerCapabilities> SERVER_PLAYER_CAPABILITIES = serverToClient(new Identifier("unicopia", "player_capabilities"), MsgPlayerCapabilities::new);
    MPacketType<MsgOtherPlayerCapabilities> SERVER_OTHER_PLAYER_CAPABILITIES = serverToClients(new Identifier("unicopia", "other_player_capabilities"), MsgOtherPlayerCapabilities::new);
    CPacketType<MsgSpawnProjectile> SERVER_SPAWN_PROJECTILE = serverToClient(new Identifier("unicopia", "projectile_entity"), MsgSpawnProjectile::new);

    static void bootstrap() { }

    static <T extends Packet> SPacketType<T> clientToServer(Identifier id, Function<PacketByteBuf, T> factory) {
        ServerSidePacketRegistry.INSTANCE.register(id, (context, buffer) -> factory.apply(buffer).handleOnMain(context));
        return () -> id;
    }

    static <T extends Packet> MPacketType<T> serverToClients(Identifier id, Function<PacketByteBuf, T> factory) {
        ClientSidePacketRegistry.INSTANCE.register(id, (context, buffer) -> factory.apply(buffer).handleOnMain(context));
        return () -> id;
    }

    static <T extends Packet> CPacketType<T> serverToClient(Identifier id, Function<PacketByteBuf, T> factory) {
        ClientSidePacketRegistry.INSTANCE.register(id, (context, buffer) -> factory.apply(buffer).handleOnMain(context));
        return () -> id;
    }

    interface MPacketType<T extends Packet> {
        Identifier getId();

        default void send(PlayerEntity sender, T packet) {
            sender.world.getPlayers().forEach(player -> {
                if (player != null) {
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, getId(), packet.toBuffer());
                }
            });
        }

        default net.minecraft.network.Packet<?> toPacket(T packet) {
            return ServerSidePacketRegistry.INSTANCE.toPacket(getId(), packet.toBuffer());
        }
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

        default void handleOnMain(PacketContext context) {
            context.getTaskQueue().execute(() -> handle(context));
        }

        default PacketByteBuf toBuffer() {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            toBuffer(buf);
            return buf;
        }
    }
}
