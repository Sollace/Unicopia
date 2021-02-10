package com.minelittlepony.unicopia.client.render;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;

public final class CanvasCompat {
    private CanvasCompat() {}
    public static VertexConsumer getGlowingConsumer(boolean glowing, VertexConsumerProvider renderContext, RenderLayer layer) {
        if (!glowing || !RendererAccess.INSTANCE.hasRenderer() || !FabricLoader.getInstance().isModLoaded("canvas")) {
            return ItemRenderer.getArmorGlintConsumer(renderContext, layer, false, false);
        }

        return CanvasCompatImpl.getGlowingConsumer(glowing, renderContext, layer);
    }
}
