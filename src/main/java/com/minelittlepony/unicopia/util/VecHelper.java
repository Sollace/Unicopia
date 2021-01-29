package com.minelittlepony.unicopia.util;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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


    /**
     * Gets all entities within a given range from the player.
     */
    static List<Entity> findInReach(PlayerEntity player, double reach, @Nullable Predicate<? super Entity> predicate) {
        Vec3d look = player.getCameraPosVec(1).multiply(reach);

        return player.world.getOtherEntities(player, player
                .getBoundingBox()
                .expand(look.x, look.y, look.z)
                .expand(1, 1, 1), predicate);
    }
}
