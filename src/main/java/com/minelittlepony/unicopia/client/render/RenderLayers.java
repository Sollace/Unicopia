package com.minelittlepony.unicopia.client.render;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.minelittlepony.common.util.Color;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public final class RenderLayers extends RenderLayer {
    private RenderLayers() {
        super(null, null, null, 0, false, false, null, null);
    }
    private static final List<RenderLayer> BLOCK_DESTRUCTION_STAGE_LAYERS = ModelLoader.BLOCK_DESTRUCTION_STAGE_TEXTURES.stream().map(texture -> {
        RenderPhase.Texture texture2 = new RenderPhase.Texture(texture, false, false);
        return (RenderLayer)RenderLayer.of("alpha_crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256,
                MultiPhaseParameters.builder()
                .program(CRUMBLING_PROGRAM)
                .texture(texture2)
                .writeMaskState(COLOR_MASK)
                .cull(DISABLE_CULLING)
                .depthTest(EQUAL_DEPTH_TEST)
                .transparency(CRUMBLING_TRANSPARENCY)
                .build(false));
    }).toList();

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
                .texturing(solid(color))
            .build(false));
    });
    private static final RenderLayer MAGIC_COLORED = getMagicColored(Color.argbToHex(0.6F, 0.8F, 0.9F, 1));

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

    public static RenderLayer getCrumbling(int stage) {
        return BLOCK_DESTRUCTION_STAGE_LAYERS.get(stage);
    }

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

    private static Texturing solid(int color) {
        final float r = Color.r(color);
        final float g = Color.g(color);
        final float b = Color.b(color);
        final float a = Color.a(color);
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
            this.alpha = Color.a(color);
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
