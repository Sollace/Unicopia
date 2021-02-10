package com.minelittlepony.unicopia.client.render;

import grondag.canvas.material.state.MaterialFinderImpl;
import grondag.canvas.material.state.RenderLayerHelper;
import grondag.frex.api.material.FrexVertexConsumerProvider;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;

final class CanvasCompat {
    static VertexConsumer getGlowingConsumer(boolean glowing, VertexConsumerProvider renderContext, RenderLayer layer) {
        if (!glowing || !RendererAccess.INSTANCE.hasRenderer() || !FabricLoader.getInstance().isModLoaded("canvas")) {
            return ItemRenderer.getArmorGlintConsumer(renderContext, layer, false, false);
        }

        if (!(renderContext instanceof FrexVertexConsumerProvider)) {
            return ItemRenderer.getArmorGlintConsumer(renderContext, layer, false, false);
        }

        return ((FrexVertexConsumerProvider)renderContext).getConsumer(MaterialFinderImpl.threadLocal()
                    .copyFrom(RenderLayerHelper.copyFromLayer(layer))
                    .emissive(true)
                    .find());
    }
}
