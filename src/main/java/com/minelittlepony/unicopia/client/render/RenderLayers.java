package com.minelittlepony.unicopia.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

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

    public static RenderLayer magic() {
        return MAGIC;
    }
}
