package com.minelittlepony.unicopia.client.render;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public final class RenderLayers extends RenderLayer {
    private RenderLayers() {
        super(null, null, null, 0, false, false, null, null);
    }

    private static final RenderLayer MAGIC_GLOW = of("uni_shield", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS, 256, true, true, MultiPhaseParameters.builder()
            .shader(COLOR_SHADER)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .target(TRANSLUCENT_TARGET)
        .build(false));

    private static final RenderLayer FAIRY = of("fairy", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS, 256, true, true, MultiPhaseParameters.builder()
            .shader(COLOR_SHADER)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .target(TRANSLUCENT_TARGET)
            .texturing(solid(0.8F, 0.9F, 1, 0.6F))
        .build(false));

    public static RenderLayer getMagicGlow() {
        return MAGIC_GLOW;
    }

    public static RenderLayer getFairy() {
        return FAIRY;
    }

    private static Texturing solid(float r, float g, float b, float a) {
        return new Texturing("solid", () -> {
            RenderSystem.setShaderColor(r, g, b, a);
        }, () -> {
            RenderSystem.setShaderColor(1, 1, 1, 1);
        });
    }
}
