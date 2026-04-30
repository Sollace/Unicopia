package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;

public interface EntityRenderRedispatcher<E extends Entity> {
    void render(E entity, double x, double y, double z, VertexConsumerProvider vertexConsumers, int light, EntityRenderer<? super E, ?> renderer);

    default void render(E entity, double x, double y, double z, VertexConsumerProvider vertices, int light) {
        render(entity, x, y, z, vertices, Untyped.cast(MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity)));
    }
}
