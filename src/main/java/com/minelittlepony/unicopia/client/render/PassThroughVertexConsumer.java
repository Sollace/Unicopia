package com.minelittlepony.unicopia.client.render;

import net.minecraft.client.render.VertexConsumer;

public class PassThroughVertexConsumer implements VertexConsumer {
    private static final ColorFix COLOR = VertexConsumer::color;
    private static final FUvFix TEXTURE = VertexConsumer::texture;
    private static final IUvFix OVERLAY = VertexConsumer::overlay;
    private static final IUvFix LIGHT = VertexConsumer::light;

    private final VertexConsumer parent;

    private final ColorFix colorFix;
    private final FUvFix textureFix;
    private final IUvFix overlayFix;
    private final IUvFix lightFix;

    public static VertexConsumer of(VertexConsumer parent, Parameters parameters) {
        return new PassThroughVertexConsumer(parent, parameters);
    }

    PassThroughVertexConsumer(VertexConsumer parent, Parameters parameters) {
        this.parent = parent;
        colorFix = parameters.colorFix;
        textureFix = parameters.textureFix;
        overlayFix = parameters.overlayFix;
        lightFix = parameters.lightFix;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        parent.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        colorFix.apply(parent, r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        textureFix.apply(parent, u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        overlayFix.apply(parent, u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        lightFix.apply(parent, u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        parent.normal(x, y, z);
        return this;
    }

    @Override
    public void next() {
        parent.next();
    }

    @Override
    public void fixedColor(int r, int g, int b, int a) {
        parent.fixedColor(r, g, b, a);
    }

    @Override
    public void unfixColor() {
        parent.unfixColor();
    }

    public static class Parameters {
        private ColorFix colorFix = COLOR;
        private FUvFix textureFix = TEXTURE;
        private IUvFix overlayFix = OVERLAY;
        private IUvFix lightFix = LIGHT;

        public Parameters color(ColorFix fix) {
            colorFix = fix;
            return this;
        }

        public Parameters texture(FUvFix fix) {
            textureFix = fix;
            return this;
        }

        public Parameters overlay(IUvFix fix) {
            overlayFix = fix;
            return this;
        }

        public Parameters light(IUvFix fix) {
            lightFix = fix;
            return this;
        }
    }

    public interface PosFix {
        void apply(VertexConsumer consumer, float x, float y, float z);
    }
    public interface ColorFix {
        void apply(VertexConsumer consumer, int r, int g, int b, int a);
    }
    public interface FUvFix {
        void apply(VertexConsumer consumer, float u, float v);
    }
    public interface IUvFix {
        void apply(VertexConsumer consumer, int u, int v);
    }
}
