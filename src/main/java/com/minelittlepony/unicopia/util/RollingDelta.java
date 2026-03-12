package com.minelittlepony.unicopia.util;

import org.joml.Vector3d;

import net.minecraft.util.math.Vec3d;

public class RollingDelta {

    private int frame;
    private final Vector3d[] snapshots;
    private final Vector3d average = new Vector3d();

    private Vec3d value = Vec3d.ZERO;
    private double squaredLength;
    private double length;
    private double horLengthSquared;

    public RollingDelta(int ticks) {
        this.snapshots = new Vector3d[ticks];
    }

    public void update(Vec3d snapshot) {
        if (snapshots[frame] == null) {
            snapshots[frame] = new Vector3d();
        }
        snapshots[frame].set(snapshot.x, snapshot.y, snapshot.z);
        frame = (frame + 1) % snapshots.length;
        average.set(0, 0, 0);
        float count = 0;
        for (int i = 0; i < snapshots.length; i++) {
            if (snapshots[i] != null) {
                average.add(snapshots[i]);
                count ++;
            }
        }
        average.mul(1 / count);
        value = new Vec3d(average.x, average.y, average.z);
        squaredLength = value.lengthSquared();
        horLengthSquared = value.horizontalLengthSquared();
        length = Math.sqrt(squaredLength);
    }

    public Vec3d read() {
        return value;
    }

    public double length() {
        return length;
    }

    public double lengthSquared() {
        return squaredLength;
    }

    public double horLengthSquared() {
        return horLengthSquared;
    }
}
