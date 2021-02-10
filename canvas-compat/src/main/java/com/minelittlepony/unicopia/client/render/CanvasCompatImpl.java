package com.minelittlepony.unicopia.client.render;

import grondag.canvas.material.state.MaterialFinderImpl;
import grondag.canvas.material.state.RenderLayerHelper;
import grondag.frex.api.material.FrexVertexConsumerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;

final class CanvasCompatImpl {
    private CanvasCompatImpl() {}
    public static VertexConsumer getGlowingConsumer(boolean glowing, VertexConsumerProvider renderContext, RenderLayer layer) {
        if (!(renderContext instanceof FrexVertexConsumerProvider)) {
            return ItemRenderer.getArmorGlintConsumer(renderContext, layer, false, false);
        }

        return ((FrexVertexConsumerProvider)renderContext).getConsumer(MaterialFinderImpl.threadLocal()
                    .copyFrom(RenderLayerHelper.copyFromLayer(layer))
                    .emissive(true)
                    .find());
    }
}
