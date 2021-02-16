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

    public Vec3d toImmutable() {
        return new Vec3d(x, y, z);
    }
}
