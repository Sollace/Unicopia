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

    private static final RenderLayer MAGIC = RenderLayer.of("mlp_magic_glow", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, 7, 256, RenderLayer.MultiPhaseParameters.builder()
            .texture(NO_TEXTURE)
            .writeMaskState(COLOR_MASK)
            .transparency(LIGHTNING_TRANSPARENCY)
            .lightmap(DISABLE_LIGHTMAP)
            .cull(DISABLE_CULLING)
            .build(false));

    public static RenderLayer entityNoLighting(Identifier tex) {
        return RenderLayer.of("entity_cutout_no_cull_no_lighting", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(tex, false, false))
                .transparency(RenderLayer.NO_TRANSPARENCY)
                .diffuseLighting(RenderLayer.ENABLE_DIFFUSE_LIGHTING)
                .alpha(RenderLayer.ONE_TENTH_ALPHA)
                .cull(RenderLayer.DISABLE_CULLING)
                .lightmap(RenderLayer.DISABLE_LIGHTMAP)
                .overlay(RenderLayer.ENABLE_OVERLAY_COLOR)
                .build(true));
    }

    public static RenderLayer cloud(Identifier tex) {
        return RenderLayer.of("cloud", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, false, RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(tex, false, false))
                .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
                .diffuseLighting(RenderLayer.ENABLE_DIFFUSE_LIGHTING)
                .alpha(RenderLayer.ONE_TENTH_ALPHA)
                .cull(RenderLayer.DISABLE_CULLING)
                .lightmap(RenderLayer.DISABLE_LIGHTMAP)
                .overlay(RenderLayer.ENABLE_OVERLAY_COLOR)
                .build(true));
    }

    public static RenderLayer magic() {
        return MAGIC;
    }
}
