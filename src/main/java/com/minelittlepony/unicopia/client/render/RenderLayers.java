package com.minelittlepony.unicopia.client.render;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.minelittlepony.common.util.Color;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public final class RenderLayers extends RenderLayer {
    private RenderLayers() {
        super(null, null, null, 0, false, false, null, null);
    }

    private static final RenderLayer MAGIC_NO_COLOR = of("magic_no_color", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS, 256, true, true, MultiPhaseParameters.builder()
            .program(COLOR_PROGRAM)
            .transparency(TRANSLUCENT_TRANSPARENCY)
            .target(TRANSLUCENT_TARGET)
        .build(false));

    private static final Function<Integer, RenderLayer> MAGIC_COLORIN_FUNC = Util.memoize(color -> {
        return of("magic_colored_" + color,
                    VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                    VertexFormat.DrawMode.QUADS, 256, true, true,
            MultiPhaseParameters.builder()
                .program(COLOR_PROGRAM)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .layering(VIEW_OFFSET_Z_LAYERING)
               // .target(TRANSLUCENT_TARGET)
                .texturing(solid(Color.r(color), Color.g(color), Color.b(color), 0.6F))
            .build(false));
    });
    private static final RenderLayer MAGIC_COLORED = getMagicColored(Color.argbToHex(1, 0.8F, 0.9F, 1));


    private static final BiFunction<Identifier, Integer, RenderLayer> MAGIC_TINT_FUNC = Util.memoize((texture, color) -> {
        return of("magic_tint_" + color,
                    VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                    VertexFormat.DrawMode.QUADS, 256, true, true,
            MultiPhaseParameters.builder()
                .texture(new Colored(texture, color))
                .program(EYES_PROGRAM)
                .writeMaskState(COLOR_MASK)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .layering(VIEW_OFFSET_Z_LAYERING)
                .cull(DISABLE_CULLING)
            .build(false));
    });

    public static RenderLayer getMagicNoColor() {
        return MAGIC_NO_COLOR;
    }

    public static RenderLayer getMagicColored() {
        return MAGIC_COLORED;
    }

    public static RenderLayer getMagicColored(int color) {
        return MAGIC_COLORIN_FUNC.apply(color);
    }

    public static RenderLayer getMagicColored(Identifier texture, int color) {
        return MAGIC_TINT_FUNC.apply(texture, color);
    }

    private static Texturing solid(float r, float g, float b, float a) {
        return new Texturing("solid", () -> {
            RenderSystem.setShaderColor(r, g, b, a);
        }, () -> {
            RenderSystem.setShaderColor(1, 1, 1, 1);
        });
    }


    private static class Colored extends Texture {

        private final float red;
        private final float green;
        private final float blue;
        private final float alpha;

        public Colored(Identifier texture, int color) {
            super(texture, false, false);
            this.red = Color.r(color);
            this.green = Color.g(color);
            this.blue = Color.b(color);
            this.alpha = 0.8F;
        }

        @Override
        public void startDrawing() {
            RenderSystem.setShaderColor(red, green, blue, alpha);
            super.startDrawing();
        }

        @Override
        public void endDrawing() {
            super.endDrawing();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other)
                    && ((Colored)other).red == red
                    && ((Colored)other).green == green
                    && ((Colored)other).blue == blue
                    && ((Colored)other).alpha == alpha;
        }
    }
}
