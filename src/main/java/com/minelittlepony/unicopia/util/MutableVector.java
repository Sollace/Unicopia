package com.minelittlepony.unicopia.util;

import net.minecraft.util.math.Vec3d;

public class MutableVector {

    public double x;
    public double y;
    public double z;

    public MutableVector(Vec3d vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
    }

    public void fromImmutable(Vec3d vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
    }

    public void multiply(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
    }

    public void add(Vec3d vector) {
        add(vector.x, vector.y, vector.z);
    }

    public void add(Vec3d vector, float scale) {
        add(vector.x, vector.y, vector.z, scale);
    }

    public void add(double x, double y, double z, float scale) {
        add(x * scale, y * scale, z * scale);
    }

    public void add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public double horizontalLengthSquared() {
        return x * x + z * z;
    }

    public double verticalLengthSquared() {
        return y * y;
    }

    public double lengthSquared() {
        return verticalLengthSquared() + horizontalLengthSquared();
    }

    public Vec3d toImmutable() {
        return new Vec3d(x, y, z);
    }
}
