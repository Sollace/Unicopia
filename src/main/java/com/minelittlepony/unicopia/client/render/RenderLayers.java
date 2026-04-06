package com.minelittlepony.unicopia.client.render;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.minelittlepony.unicopia.client.render.shader.URenderPipelines;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;

import static net.minecraft.client.render.RenderLayer.*;

public final class RenderLayers {

    public static final int DEFAULT_MAGIC_COLOR = ColorHelper.fromFloats(0.6F, 0.8F, 0.9F, 1);

    private static final List<RenderLayer> BLOCK_DESTRUCTION_STAGE_LAYERS = ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS;/*BLOCK_DESTRUCTION_STAGE_TEXTURES.stream().map(texture -> {
        RenderPhase.Texture texture2 = new RenderPhase.Texture(texture, TriState.DEFAULT, false);
        return (RenderLayer)RenderLayer.of("alpha_crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256,
                MultiPhaseParameters.builder()
                .program(CRUMBLING_PROGRAM)
                .texture(texture2)
                .writeMaskState(COLOR_MASK)
                .cull(DISABLE_CULLING)
                .depthTest(EQUAL_DEPTH_TEST)
                .transparency(CRUMBLING_TRANSPARENCY)
                .build(false));
    }).toList();*/

    private static final RenderLayer MAGIC_NO_COLOR = of("magic_no_color", 256, true, true, URenderPipelines.TRANSLUCENT_SOLID, MultiPhaseParameters.builder()
            .target(TRANSLUCENT_TARGET)
        .build(false));

    private static final RenderLayer MAGIC_SHIELD = of("magic_shield", 256, true, true, URenderPipelines.TRANSLUCENT_SOLID_NO_CULL, MultiPhaseParameters.builder()
            .target(TRANSLUCENT_TARGET)
        .build(false));

    private static final Function<Integer, RenderLayer> MAGIC_COLORIN_FUNC = Util.memoize(color -> {
        return of("magic_colored_" + color, 1536, true, true, URenderPipelines.TRANSLUCENT_SOLID_NO_CULL, MultiPhaseParameters.builder()
                .layering(VIEW_OFFSET_Z_LAYERING)
               // .target(TRANSLUCENT_TARGET)
                .texturing(solid(color))
            .build(false));
    });
    private static final RenderLayer MAGIC_COLORED = getMagicColored(DEFAULT_MAGIC_COLOR);

    private static final BiFunction<Identifier, Integer, RenderLayer> MAGIC_TINT_FUNC = Util.memoize((texture, color) -> {
        return of("magic_tint_" + color, 1536, true, true, URenderPipelines.TRANSLUCENT_SOLID_NO_CULL, MultiPhaseParameters.builder()
                .texture(new Colored(texture, color))
                .layering(VIEW_OFFSET_Z_LAYERING)
            .build(false));
    });

    private static final Function<Identifier, RenderLayer> PORTAL = Util.memoize(texture -> of("portal", 256, false, false, URenderPipelines.RENDERTYPE_PORTAL_SURFACE, MultiPhaseParameters.builder()
            .texture(new Texture(texture, TriState.FALSE, false))
            .target(TRANSLUCENT_TARGET)
        .build(false)));

    public static RenderLayer getCrumbling(int stage) {
        return BLOCK_DESTRUCTION_STAGE_LAYERS.get(stage);
    }

    public static RenderLayer getMagicNoColor() {
        return MAGIC_NO_COLOR;
    }

    public static RenderLayer getMagicShield() {
        return MAGIC_SHIELD;
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

    public static RenderLayer getPortal(Identifier texture) {
        return PORTAL.apply(texture);
    }

    private static Texturing solid(int color) {
        final float r = ColorHelper.getRedFloat(color);
        final float g = ColorHelper.getGreenFloat(color);
        final float b = ColorHelper.getBlueFloat(color);
        final float a = ColorHelper.getAlphaFloat(color);
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
            super(texture, TriState.FALSE, false);
            this.red = ColorHelper.getRedFloat(color);
            this.green = ColorHelper.getGreenFloat(color);
            this.blue = ColorHelper.getBlueFloat(color);
            this.alpha = ColorHelper.getAlphaFloat(color);
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
