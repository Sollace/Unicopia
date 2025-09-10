package com.minelittlepony.unicopia.util.serialization;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;

public class ServerBoundByteBuf extends RegistryByteBuf {

    private final MinecraftServer server;

    public ServerBoundByteBuf(MinecraftServer server, ByteBuf buf, DynamicRegistryManager registryManager) {
        super(buf, registryManager);
        this.server = server;
    }

    public ServerBoundByteBuf(MinecraftServer server, RegistryByteBuf buf) {
        super(buf, buf.getRegistryManager());
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }

}
