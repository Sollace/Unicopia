package com.minelittlepony.unicopia.util;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface VecHelper {

    static Predicate<Entity> inRange(Vec3d center, double range) {
        double rad = Math.pow(range, 2);
        return e -> {
            return e.squaredDistanceTo(center) <= rad
                || e.squaredDistanceTo(center.getX(), center.getY() - e.getStandingEyeHeight(), center.getZ()) <= rad;
        };
    }

    static List<Entity> findInRange(@Nullable Entity origin, World w, Vec3d pos, double radius, @Nullable Predicate<Entity> predicate) {
        return w.getOtherEntities(origin, Box.method_29968(pos).expand(radius), predicate == null ? inRange(pos, radius) : inRange(pos, radius).and(predicate));
    }
}
