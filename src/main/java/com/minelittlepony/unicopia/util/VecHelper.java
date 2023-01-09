package com.minelittlepony.unicopia.util;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;

public interface VecHelper {

    static Vec3d divide(Vec3d one, Vec3d two) {
        return new Vec3d(one.x / two.x, one.y / two.y, one.z / two.z);
    }

    static Vec3d supply(DoubleSupplier rng) {
        return new Vec3d(rng.getAsDouble(), rng.getAsDouble(), rng.getAsDouble());
    }

    static Predicate<Entity> inRange(Vec3d center, double range) {
        double rad = Math.pow(range, 2);
        return e -> {
            return e.squaredDistanceTo(center) <= rad
                || e.squaredDistanceTo(center.getX(), center.getY() - e.getStandingEyeHeight(), center.getZ()) <= rad;
        };
    }

    static List<Entity> findInRange(@Nullable Entity origin, EntityView w, Vec3d pos, double radius, @Nullable Predicate<Entity> predicate) {
        double diameter = radius * 2;
        return w.getOtherEntities(origin, Box.of(pos, diameter, diameter, diameter), predicate == null ? inRange(pos, radius) : inRange(pos, radius).and(predicate));
    }

    static List<Entity> findInRange(@Nullable Entity origin, EntityView w, Vec3d pos, double radius) {
        double diameter = radius * 2;
        return w.getOtherEntities(origin, Box.of(pos, diameter, diameter, diameter), inRange(pos, radius));
    }
}
