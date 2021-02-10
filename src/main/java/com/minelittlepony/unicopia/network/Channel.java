package com.minelittlepony.unicopia.network;

import java.util.function.Function;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Channel {

    SPacketType<MsgPlayerAbility<?>> CLIENT_PLAYER_ABILITY = clientToServer(new Identifier("unicopia", "player_ability"), MsgPlayerAbility::new);
    SPacketType<MsgRequestCapabilities> CLIENT_REQUEST_CAPABILITIES = clientToServer(new Identifier("unicopia", "request_capabilities"), MsgRequestCapabilities::new);

    CPacketType<MsgPlayerCapabilities> SERVER_PLAYER_CAPABILITIES = serverToClient(new Identifier("unicopia", "player_capabilities"), MsgPlayerCapabilities::new);
    MPacketType<MsgOtherPlayerCapabilities> SERVER_OTHER_PLAYER_CAPABILITIES = serverToClients(new Identifier("unicopia", "other_player_capabilities"), MsgOtherPlayerCapabilities::new);
    CPacketType<MsgSpawnProjectile> SERVER_SPAWN_PROJECTILE = serverToClient(new Identifier("unicopia", "projectile_entity"), MsgSpawnProjectile::new);

    CPacketType<MsgBlockDestruction> SERVER_BLOCK_DESTRUCTION = serverToClient(new Identifier("unicopia", "block_destruction"), MsgBlockDestruction::new);

    static void bootstrap() { }

    static <T extends Packet> SPacketType<T> clientToServer(Identifier id, Function<PacketByteBuf, T> factory) {
        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, responder) -> {
            factory.apply(buffer).handleOnMain(server, player);
        });
        return () -> id;
    }

    static <T extends Packet> CPacketType<T> serverToClient(Identifier id, Function<PacketByteBuf, T> factory) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                factory.apply(buffer).handleOnMain(client, client.player);
            });
        }
        return () -> id;
    }

    static <T extends Packet> MPacketType<T> serverToClients(Identifier id, Function<PacketByteBuf, T> factory) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                factory.apply(buffer).handleOnMain(client, client.player);
            });
        }
        return () -> id;
    }

    interface MPacketType<T extends Packet> {
        Identifier getId();

        default void send(World world, T packet) {
            world.getPlayers().forEach(player -> {
                if (player != null) {
                    ServerPlayNetworking.send((ServerPlayerEntity)player, getId(), packet.toBuffer());
                }
            });
        }
    }

    interface CPacketType<T extends Packet> {
        Identifier getId();

        default void send(PlayerEntity recipient, T packet) {
            ServerPlayNetworking.send((ServerPlayerEntity)recipient, getId(), packet.toBuffer());
        }

        default net.minecraft.network.Packet<?> toPacket(T packet) {
            return ServerPlayNetworking.createS2CPacket(getId(), packet.toBuffer());
        }
    }

    interface SPacketType<T extends Packet> {
        Identifier getId();

        default void send(T packet) {
            if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
                throw new RuntimeException("Client packet send called by the server");
            }
            ClientPlayNetworking.send(getId(), packet.toBuffer());
        }
    }

    interface Packet {
        void handle(PlayerEntity sender);

        void toBuffer(PacketByteBuf buffer);

        default void handleOnMain(ThreadExecutor<?> server, PlayerEntity player) {
            server.execute(() -> handle(player));
        }

        default PacketByteBuf toBuffer() {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            toBuffer(buf);
            return buf;
        }
    }
}
