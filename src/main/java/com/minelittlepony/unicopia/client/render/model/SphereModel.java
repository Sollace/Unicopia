package com.minelittlepony.unicopia.client.render.model;

import java.util.function.Consumer;

import org.joml.Vector4f;

import com.minelittlepony.unicopia.client.gui.DrawableUtil;

import net.minecraft.util.math.MathHelper;

public class SphereModel extends BakedModel {
    public static final SphereModel SPHERE = new SphereModel(40, 40, DrawableUtil.TAU);
    public static final SphereModel DISK = new SphereModel(40, 2, DrawableUtil.PI);
    public static final SphereModel HEXAGON = new SphereModel(3, 2, DrawableUtil.TAU);
    public static final SphereModel PRISM = new SphereModel(6, 6, DrawableUtil.TAU);

    public SphereModel(double rings, double sectors, double azimuthRange) {
        double zenithIncrement = DrawableUtil.PI / rings;
        double azimuthIncrement = DrawableUtil.TAU / sectors;
        compileVertices(azimuthRange, zenithIncrement, azimuthIncrement, this::addVertex);
    }

    private static void compileVertices(double azimuthRange, double zenithIncrement, double azimuthIncrement, Consumer<Vector4f> collector) {
        Vector4f vector = new Vector4f();
        for (double zenith = 0; zenith < DrawableUtil.PI; zenith += zenithIncrement) {
            for (double azimuth = 0; azimuth < azimuthRange; azimuth += azimuthIncrement) {
                collector.accept(convertToCartesianCoord(vector, 1, zenith, azimuth));
                collector.accept(convertToCartesianCoord(vector, 1, zenith + zenithIncrement, azimuth));
                collector.accept(convertToCartesianCoord(vector, 1, zenith + zenithIncrement, azimuth + azimuthIncrement));
                collector.accept(convertToCartesianCoord(vector, 1, zenith, azimuth + azimuthIncrement));
            }
        }
    }

    public static Vector4f convertToCartesianCoord(Vector4f output, double r, double theta, double phi) {
        float st = MathHelper.sin((float)theta);
        output.set(
            (float)(r * st * MathHelper.cos((float)phi)),
            (float)(r * st * MathHelper.sin((float)phi)),
            (float)(r * MathHelper.cos((float)theta)),
            1
        );
        return output;
    }
}
