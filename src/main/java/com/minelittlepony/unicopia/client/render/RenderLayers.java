package com.minelittlepony.unicopia.client.render;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;

public final class RenderLayers extends RenderLayer {

    private RenderLayers(String name, VertexFormat vertexFormat, DrawMode drawMode, int expectedBufferSize,
            boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    private static final RenderLayer MAGIC_GLOW = RenderLayer.of("uni_shield", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS, 256, true, true, RenderLayer.MultiPhaseParameters.builder()
            .shader(COLOR_SHADER)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .target(TRANSLUCENT_TARGET)
        .build(false));

    private static final RenderLayer FAIRY = RenderLayer.of("fairy", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS, 256, true, true, RenderLayer.MultiPhaseParameters.builder()
            .shader(COLOR_SHADER)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .target(TRANSLUCENT_TARGET)
            .texturing(solid(120, 120, 0, 0.6F))
        .build(false));

    public static RenderLayer getMagicGlow() {
        return MAGIC_GLOW;
    }

    public static RenderLayer getFairy() {
        return FAIRY;
    }

    public static Texturing solid(float r, float g, float b, float a) {
        return new Texturing("solid", () -> {
            RenderSystem.setShaderColor(r, g, b, a);
        }, () -> {
            RenderSystem.setShaderColor(1, 1, 1, 1);
        });
    }
}
