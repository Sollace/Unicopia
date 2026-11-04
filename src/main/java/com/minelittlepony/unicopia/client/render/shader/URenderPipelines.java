package com.minelittlepony.unicopia.client.render.shader;

import com.minelittlepony.unicopia.Unicopia;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;

import static net.minecraft.client.gl.RenderPipelines.*;

public interface URenderPipelines {
    RenderPipeline TRANSLUCENT_SOLID_NO_CULL = register(
        RenderPipeline.builder(MATRICES_COLOR_FOG_SNIPPET)
            .withLocation(Unicopia.id("translucent_solid_no_cull"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withCull(false)
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .build()
        );
    RenderPipeline TRANSLUCENT_ENTITY_NO_CULL = register(
            RenderPipeline.builder(MATRICES_COLOR_FOG_SNIPPET)
                .withLocation(Unicopia.id("translucent_solid_no_cull"))
                .withVertexShader(Unicopia.id("core/silhouette"))
                .withFragmentShader(Unicopia.id("core/silhouette"))
                .withSampler("Sampler0")
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthWrite(false)
                .withCull(false)
                .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
                .build()
            );
    RenderPipeline TRANSLUCENT_SOLID = register(
        RenderPipeline.builder(MATRICES_COLOR_FOG_SNIPPET)
            .withLocation(Unicopia.id("translucent_solid"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthWrite(false)
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .build()
        );

    RenderPipeline RENDERTYPE_PORTAL_SURFACE = register(
        RenderPipeline.builder(RenderPipelines.MATRICES_SNIPPET, RenderPipelines.FOG_SNIPPET)
            .withLocation(Unicopia.id("portal_surface"))
            .withVertexShader(Unicopia.id("core/rendertype_portal_surface"))
            .withFragmentShader(Unicopia.id("core/rendertype_portal_surface"))
            .withSampler("Sampler0")
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS)
            .build()
        );

    static void bootstrap() {}
}
