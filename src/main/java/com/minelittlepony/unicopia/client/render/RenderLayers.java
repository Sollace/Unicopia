package com.minelittlepony.unicopia.client.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public final class RenderLayers extends RenderLayer {

    private RenderLayers(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize,
            boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    protected static final RenderPhase.Transparency GLOWING_TRANSPARENCY = new RenderPhase.Transparency("glowing_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                SrcFactor.CONSTANT_COLOR, DstFactor.ONE,
                SrcFactor.ONE, DstFactor.ZERO);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static RenderLayer getTintedTexturedLayer(float red, float green, float blue, float alpha) {
        return RenderLayer.of("mlp_tint_layer", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, RenderLayer.MultiPhaseParameters.builder()
                .texture(new Color(red, green, blue, alpha))
                .writeMaskState(COLOR_MASK)
                .alpha(ONE_TENTH_ALPHA)
                .transparency(GLOWING_TRANSPARENCY)
                .shadeModel(SMOOTH_SHADE_MODEL)
                .lightmap(DISABLE_LIGHTMAP)
                .overlay(DISABLE_OVERLAY_COLOR)
                .cull(DISABLE_CULLING)
                .build(true));
    }

    private static class Color extends Texture {

        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;

        public Color(float red, float green, float blue, float alpha) {
            super();
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        @Override
        public void startDrawing() {
            RenderSystem.blendColor(red, green, blue, alpha);
            super.startDrawing();
        }

        @Override
        public void endDrawing() {
            super.endDrawing();
            RenderSystem.blendColor(1, 1, 1, 1);
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other)
                    && ((Color)other).red == red
                    && ((Color)other).green == green
                    && ((Color)other).blue == blue
                    && ((Color)other).alpha == alpha;
        }
    }
}
