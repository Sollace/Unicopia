package com.minelittlepony.unicopia.client.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;

public final class RenderLayers extends RenderLayer {

    private RenderLayers(String name, VertexFormat vertexFormat, DrawMode drawMode, int expectedBufferSize,
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
        return RenderLayer.of("mlp_tint_layer", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, true, RenderLayer.MultiPhaseParameters.builder()
                .texture(new Color(red, green, blue, alpha))
                .shader(EYES_SHADER)
                .writeMaskState(COLOR_MASK)
                .depthTest(LEQUAL_DEPTH_TEST)
                .transparency(GLOWING_TRANSPARENCY)
                .lightmap(DISABLE_LIGHTMAP)
                .overlay(DISABLE_OVERLAY_COLOR)
                .cull(DISABLE_CULLING)
                .build(true));
    }

    private static class Color extends TextureBase {
        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;

        public Color(float red, float green, float blue, float alpha) {
            super(
                    () -> RenderSystem.setShaderColor(red, green, blue, alpha),
                    () -> RenderSystem.setShaderColor(1, 1, 1, 1)
            );
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
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
