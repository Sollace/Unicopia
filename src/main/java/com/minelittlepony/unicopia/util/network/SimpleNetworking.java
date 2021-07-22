package com.minelittlepony.unicopia.util.network;

import java.util.function.Function;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A simplified, side-agnostic, and declaritive wrapper around {@link ServerPlayNetworking} and {@link ClientPlayNetworking}
 * designed to bring networking in line with the declaritive/registered nature of other parts of Mojang's echosystem.
 * <p>
 * It is safe to call these methods from either the client or the server, so modders can implement a
 * single static <code>PacketTypes</code> class with which they can easily send packets without worrying
 * about the complexities of the network thread, which side to register a global receiver on,
 * which method to use to send, or even whether their receiver is registered for a given player or not.
 * <p>
 * All of the above is handled in a black-box style by this class.
 * <p>
 * <ul>
 * <li>Packets are automatically registered on the appropriate sides.</li>
 * <li>Sending is done in the same way by calling `send` on your packet type.</li>
 * <li>Your packet's <code>handle</code> method is executed on the main thread where it is safe to interact with the world.</li>
 */
public final class SimpleNetworking {
    private SimpleNetworking() {throw new RuntimeException("new SimpleNetworking()");}
    /**
     * Registers a packet type for transmisison to the server.
     * <p>
     * The returned handle can be used by the client to send messages to the active minecraft server.
     * <p>
     *
     * @param <T>     The type of packet to implement
     * @param id      The message's unique used for serialization
     * @param factory A constructor returning new instances of the packet type
     *
     * @return A registered PacketType
     */
    public static <T extends Packet<ServerPlayerEntity>> C2SPacketType<T> clientToServer(Identifier id, Function<PacketByteBuf, T> factory) {
        ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, responder) -> {
            T packet = factory.apply(buffer);
            server.execute(() -> packet.handle(player));
        });
        return () -> id;
    }
    /**
     * Registers a packet type for transmission to the client.
     *
     * The returned handle can be used by the server to send messages to a given recipient.
     *
     * @param <T>     The type of packet to implement
     * @param id      The message's unique used for serialization
     * @param factory A constructor returning new instances of the packet type
     *
     * @return A registered PacketType
     */
    public static <T extends Packet<PlayerEntity>> S2CPacketType<T> serverToClient(Identifier id, Function<PacketByteBuf, T> factory) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(id, factory);
        }
        return () -> id;
    }
    /**
     * Registers a packet type for transmission to all clients.
     *
     * The returned handle can be used by the server to broadcast a message to all connected clients in a given dimension.
     *
     * @param <T>     The type of packet to implement
     * @param id      The message's unique used for serialization
     * @param factory A constructor returning new instances of the packet type
     *
     * @return A registered PacketType
     */
    public static <T extends Packet<PlayerEntity>> S2CBroadcastPacketType<T> serverToClients(Identifier id, Function<PacketByteBuf, T> factory) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(id, factory);
        }
        return () -> id;
    }
    // Fabric's APIs are not side-agnostic.
    // We punt this to a separate class file to keep it from being eager-loaded on a server environment.
    private static final class ClientProxy {
        private ClientProxy() {throw new RuntimeException("new ClientProxy()");}
        public static <T extends Packet<PlayerEntity>> void register(Identifier id, Function<PacketByteBuf, T> factory) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                T packet = factory.apply(buffer);
                client.execute(() -> packet.handle(client.player));
            });
        }
    }
}
