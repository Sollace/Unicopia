package com.minelittlepony.unicopia.client.render.model;

import org.joml.Vector4f;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class VertexLightSource {
    private final ClientWorld world;
    private final Long2ObjectMap<Integer> lightCache = new Long2ObjectOpenHashMap<>();

    public VertexLightSource(ClientWorld world) {
        this.world = world;
    }

    public void tick() {
        lightCache.clear();
    }

    public int getLight(Vector4f vertexPosition, int light) {
        return lightCache.computeIfAbsent(getBlockPosition(vertexPosition), this::getLight);
    }

    @SuppressWarnings("deprecation")
    private int getLight(long p) {
        final BlockPos pos = BlockPos.fromLong(p);
        return world.isChunkLoaded(pos) ? WorldRenderer.getLightmapCoordinates(world, pos) : 0;
    }

    private long getBlockPosition(Vector4f vertexPosition) {
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
        return BlockPos.asLong(
                MathHelper.floor(cameraPos.x + vertexPosition.x),
                MathHelper.floor(cameraPos.y + vertexPosition.y),
                MathHelper.floor(cameraPos.z + vertexPosition.z)
        );
    }
}
