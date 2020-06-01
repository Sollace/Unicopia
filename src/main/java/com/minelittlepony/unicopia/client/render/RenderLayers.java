package com.minelittlepony.unicopia.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public final class RenderLayers extends RenderLayer {

    private RenderLayers(String name, VertexFormat vertexFormat, int drawMode, int expectedBufferSize,
            boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    private static final RenderLayer MAGIC = of("mlp_magic_glow", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(NO_TEXTURE)
            .writeMaskState(COLOR_MASK)
            .transparency(LIGHTNING_TRANSPARENCY)
            .lightmap(DISABLE_LIGHTMAP)
            .cull(DISABLE_CULLING)
            .build(false));

    public static RenderLayer entityNoLighting(Identifier tex) {
        return of("entity_cutout_no_cull_no_lighting", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(tex, false, false))
                .transparency(NO_TRANSPARENCY)
                .diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
                .alpha(ONE_TENTH_ALPHA)
                .cull(DISABLE_CULLING)
                .lightmap(DISABLE_LIGHTMAP)
                .overlay(ENABLE_OVERLAY_COLOR)
                .build(true));
    }

    public static RenderLayer cloud(Identifier tex) {
        return of("cloud", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(tex, false, false))
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
                .alpha(ONE_TENTH_ALPHA)
                .lightmap(DISABLE_LIGHTMAP)
                .overlay(DISABLE_OVERLAY_COLOR)
                .cull(DISABLE_CULLING)
                .build(true));
    }

    public static RenderLayer magic() {
        return MAGIC;
    }
}
