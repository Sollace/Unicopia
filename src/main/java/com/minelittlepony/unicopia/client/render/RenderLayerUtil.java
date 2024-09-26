package com.minelittlepony.unicopia.client.render;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public interface RenderLayerUtil {
    Identifier SHADOW_TEXTURE = Identifier.ofVanilla("textures/misc/shadow.png");

    static void createUnionBuffer(Consumer<VertexConsumerProvider> action, VertexConsumerProvider vertices, Function<Identifier, RenderLayer> overlayFunction) {
        Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers();
        action.accept(layer -> {
            Identifier texture = RenderLayerUtil.getTexture(layer).orElse(null);

            if (texture == null || texture.equals(SHADOW_TEXTURE) || texture.equals(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)) {
                return vertices.getBuffer(layer);
            }
            return VertexConsumers.union(
                    vertices.getBuffer(layer),
                    immediate.getBuffer(overlayFunction.apply(texture))
            );
        });
        immediate.draw();
    }
}
