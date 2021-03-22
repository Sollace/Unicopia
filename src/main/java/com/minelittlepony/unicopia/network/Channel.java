package com.minelittlepony.unicopia.network;

import java.util.function.Function;

import com.google.common.base.Preconditions;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Channel {
    SPacketType<MsgPlayerAbility<?>> CLIENT_PLAYER_ABILITY = clientToServer(new Identifier("unicopia", "player_ability"), MsgPlayerAbility::new);
    SPacketType<MsgRequestCapabilities> CLIENT_REQUEST_CAPABILITIES = clientToServer(new Identifier("unicopia", "request_capabilities"), MsgRequestCapabilities::new);

    CPacketType<MsgPlayerCapabilities> SERVER_PLAYER_CAPABILITIES = serverToClient(new Identifier("unicopia", "player_capabilities"), MsgPlayerCapabilities::new);
    BPacketType<MsgOtherPlayerCapabilities> SERVER_OTHER_PLAYER_CAPABILITIES = serverToClients(new Identifier("unicopia", "other_player_capabilities"), MsgOtherPlayerCapabilities::new);
    CPacketType<MsgSpawnProjectile> SERVER_SPAWN_PROJECTILE = serverToClient(new Identifier("unicopia", "projectile_entity"), MsgSpawnProjectile::new);

    CPacketType<MsgBlockDestruction> SERVER_BLOCK_DESTRUCTION = serverToClient(new Identifier("unicopia", "block_destruction"), MsgBlockDestruction::new);

    static void bootstrap() { }

    static <T extends Packet> SPacketType<T> clientToServer(Identifier id, Function<PacketByteBuf, T> factory) {
        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, responder) -> {
            T packet = factory.apply(buffer);
            server.execute(() -> packet.handle(player));
        });
        return () -> id;
    }

    static <T extends Packet> CPacketType<T> serverToClient(Identifier id, Function<PacketByteBuf, T> factory) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(id, factory);
        }
        return () -> id;
    }

    static <T extends Packet> BPacketType<T> serverToClients(Identifier id, Function<PacketByteBuf, T> factory) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(id, factory);
        }
        return () -> id;
    }

    /**
     * A broadcast packet type. Sent by the server to all surrounding players.
     */
    interface BPacketType<T extends Packet> {
        Identifier getId();

        default void send(World world, T packet) {
            world.getPlayers().forEach(player -> {
                if (player instanceof ServerPlayerEntity) {
                    ServerPlayNetworking.send((ServerPlayerEntity)player, getId(), toBuffer(packet));
                }
            });
        }
    }

    /**
     * A client packet type. Sent by the server to a specific player.
     */
    interface CPacketType<T extends Packet> {
        Identifier getId();

        default void send(PlayerEntity recipient, T packet) {
            ServerPlayNetworking.send(((ServerPlayerEntity)recipient), getId(), toBuffer(packet));
        }

        default net.minecraft.network.Packet<?> toPacket(T packet) {
            return ServerPlayNetworking.createS2CPacket(getId(), toBuffer(packet));
        }
    }

    /**
     * A server packet type. Sent by the client to the server.
     */
    interface SPacketType<T extends Packet> {
        Identifier getId();

        default void send(T packet) {
            Preconditions.checkState(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT, "Client packet send called by the server");
            ClientPlayNetworking.send(getId(), toBuffer(packet));
        }
    }

    interface Packet {
        void handle(PlayerEntity sender);

        void toBuffer(PacketByteBuf buffer);
    }

    static PacketByteBuf toBuffer(Packet packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.toBuffer(buf);
        return buf;
    }

    class ClientProxy {
        static <T extends Packet> void register(Identifier id, Function<PacketByteBuf, T> factory) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                T packet = factory.apply(buffer);
                client.execute(() -> packet.handle(client.player));
            });
        }
    }
}
