package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;

public class PassThroughVertexConsumer extends VertexConsumers.Union implements VertexConsumer {
    private final Applicate<ColorFix> colorFix;
    private final Applicate<FUvFix> textureFix;
    private final Applicate<IUvFix> overlayFix;
    private final Applicate<IUvFix> lightFix;

    private PassThroughVertexConsumer(VertexConsumer parent, Parameters parameters) {
        super(new VertexConsumer[] {parent});
        colorFix = Applicate.of(parameters.colorFix, (self, r, g, b, a) -> super.color(r, g, b, a));
        textureFix = Applicate.of(parameters.textureFix, (self, u, v) -> super.texture(u, v));
        overlayFix = Applicate.of(parameters.overlayFix, (self, u, v) -> super.overlay(u, v));
        lightFix = Applicate.of(parameters.lightFix, (self, u, v) -> super.light(u, v));
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        colorFix.getFix().apply(this, r, g, b, a);
        colorFix.nested = false;
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        textureFix.getFix().apply(this, u, v);
        textureFix.nested = false;
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        overlayFix.getFix().apply(this, u, v);
        overlayFix.nested = false;
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        lightFix.getFix().apply(this, u, v);
        lightFix.nested = false;
        return this;
    }

    public static class Parameters {
        private @Nullable ColorFix colorFix;
        private @Nullable FUvFix textureFix;
        private @Nullable IUvFix overlayFix;
        private @Nullable IUvFix lightFix;

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

        public VertexConsumer build(VertexConsumer parent) {
            return new PassThroughVertexConsumer(parent, this);
        }
    }

    private static class Applicate<T> {
        public final T fix;
        public final T fallback;

        public boolean nested;

        public Applicate(T fix, T fallback) {
            this.fix = fix;
            this.fallback = fallback;
        }

        public T getFix() {
            try {
                return nested ? fallback : fix;
            } finally {
                nested = true;
            }
        }

        static <T> Applicate<T> of(@Nullable T fix, T fallback) {
            return new Applicate<>(fix == null ? fallback : fix, fallback);
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
