package com.minelittlepony.unicopia.client.render;

import java.util.function.Function;

import com.minelittlepony.common.util.Color;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Util;

public final class RenderLayers extends RenderLayer {
    private RenderLayers() {
        super(null, null, null, 0, false, false, null, null);
    }

    private static final RenderLayer MAGIC_NO_COLOR = of("magic_no_color", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS, 256, true, true, MultiPhaseParameters.builder()
            .shader(COLOR_SHADER)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .target(TRANSLUCENT_TARGET)
        .build(false));

    private static final Function<Integer, RenderLayer> MAGIC_COLORIN_FUNC = Util.memoize(color -> {
        return of("magic_colored_" + color,
                    VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                    VertexFormat.DrawMode.QUADS, 256, true, true,
            MultiPhaseParameters.builder()
                .shader(COLOR_SHADER)
                .transparency(TRANSLUCENT_TRANSPARENCY)
               // .target(TRANSLUCENT_TARGET)
                .texturing(solid(Color.r(color), Color.g(color), Color.b(color), 0.6F))
            .build(false));
    });
    private static final RenderLayer MAGIC_COLORED = getMagicColored(Color.argbToHex(1, 0.8F, 0.9F, 1));

    public static RenderLayer getMagicNoColor() {
        return MAGIC_NO_COLOR;
    }

    public static RenderLayer getMagicColored() {
        return MAGIC_COLORED;
    }

    public static RenderLayer getMagicColored(int color) {
        return MAGIC_COLORIN_FUNC.apply(color);
    }

    private static Texturing solid(float r, float g, float b, float a) {
        return new Texturing("solid", () -> {
            RenderSystem.setShaderColor(r, g, b, a);
        }, () -> {
            RenderSystem.setShaderColor(1, 1, 1, 1);
        });
    }
}
