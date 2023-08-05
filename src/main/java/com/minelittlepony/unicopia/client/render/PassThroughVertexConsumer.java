package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumers;

public class PassThroughVertexConsumer extends VertexConsumers.Union implements VertexConsumer {
    private final float[] buffer = new float[8];

    private final Applicate<ColorFix, ColorFix.VertexConsumer> colorFix;
    private final Applicate<FUvFix, FUvFix.VertexConsumer> textureFix;
    private final Applicate<IUvFix, IUvFix.VertexConsumer> overlayFix;

    private PassThroughVertexConsumer(VertexConsumer parent, Parameters parameters) {
        super(new VertexConsumer[] {parent});
        colorFix = Applicate.of(parameters.colorFix, ColorFix.NULL, super::color, (newR, newG, newB, newA) -> {
            buffer[0] = newR / 255F;
            buffer[1] = newG / 255F;
            buffer[2] = newB / 255F;
            buffer[3] = newA / 255F;
        });
        textureFix = Applicate.of(parameters.textureFix, FUvFix.NULL, super::texture, (u, v) -> {
            buffer[4] = u;
            buffer[5] = v;
        });
        overlayFix = Applicate.of(parameters.overlayFix, IUvFix.NULL, super::overlay, (u, v) -> {
            buffer[6] = u;
            buffer[7] = v;
        });
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        colorFix.getFix().apply(colorFix.setter, r, g, b, a);
        colorFix.nested = false;
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        textureFix.getFix().apply(textureFix.setter, u, v);
        textureFix.nested = false;
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        overlayFix.getFix().apply(overlayFix.setter, u, v);
        overlayFix.nested = false;
        return this;
    }

    @Override
    public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        colorFix.getFix().apply(colorFix.collector, (int)(red * 255), (int)(green * 255F), (int)(blue * 255), (int)(alpha * 255));
        colorFix.nested = false;
        textureFix.getFix().apply(textureFix.collector, u, v);
        textureFix.nested = false;
        overlayFix.getFix().apply(overlayFix.collector, overlay & 0xFFFF, overlay >> 16 & 0xFFFF);
        overlayFix.nested = false;
        super.vertex(x, y, z, buffer[0], buffer[1], buffer[2], buffer[3], buffer[4], buffer[5], (int)buffer[6] | (int)buffer[7] << 16, light, normalX, normalY, normalZ);
    }

    public static class Parameters {
        private @Nullable ColorFix colorFix;
        private @Nullable FUvFix textureFix;
        private @Nullable IUvFix overlayFix;

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

        public VertexConsumer build(VertexConsumer parent) {
            return new PassThroughVertexConsumer(parent, this);
        }
    }

    private static class Applicate<T extends Applicate.Applicatable<V>, V> {
        public final T fix;
        public final T fallback;
        public final V setter;
        public final V collector;

        public boolean nested;

        public Applicate(T fix, T fallback, V setter, V collector) {
            this.fix = fix;
            this.fallback = fallback;
            this.setter = setter;
            this.collector = collector;
        }

        public T getFix() {
            try {
                return nested ? fallback : fix;
            } finally {
                nested = true;
            }
        }

        static <T extends Applicate.Applicatable<V>, V> Applicate<T, V> of(@Nullable T fix, T fallback, V setter, V collector) {
            return new Applicate<>(fix == null ? fallback : fix, fallback, setter, collector);
        }

        interface Applicatable<T> {

        }
    }

    public interface PosFix extends Applicate.Applicatable<PosFix.VertexConsumer> {
        void apply(VertexConsumer consumer, float x, float y, float z);

        public interface VertexConsumer {
            void vertex(float x, float y, float z);
        }
    }
    public interface ColorFix extends Applicate.Applicatable<ColorFix.VertexConsumer> {
        ColorFix NULL = (self, r, g, b, a) -> self.color(r, g, b, a);
        void apply(VertexConsumer consumer, int r, int g, int b, int a);

        public interface VertexConsumer {
            void color(int r, int g, int b, int a);

            default void color(float r, float g, float b, float a) {
                color((int)(r * 255), (int)(g * 255), (int)(b * 255), (int)(a * 255));
            }
        }
    }
    public interface FUvFix extends Applicate.Applicatable<FUvFix.VertexConsumer> {
        FUvFix NULL = (self, u, v) -> self.uv(u, v);
        void apply(VertexConsumer consumer, float u, float v);

        public interface VertexConsumer {
            void uv(float u, float v);
        }
    }
    public interface IUvFix extends Applicate.Applicatable<IUvFix.VertexConsumer> {
        IUvFix NULL = (self, u, v) -> self.uv(u, v);
        void apply(VertexConsumer consumer, int u, int v);

        public interface VertexConsumer {
            void uv(int u, int v);
        }
    }
}
