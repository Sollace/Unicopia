package com.minelittlepony.unicopia.util;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface VecHelper {
    static Stream<Entity> findAllEntitiesInRange(@Nullable Entity origin, World w, BlockPos pos, double radius) {
        return w.getOtherEntities(origin, new Box(pos).expand(radius), e -> {
            double dist = Math.sqrt(e.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()));
            double dist2 = Math.sqrt(e.squaredDistanceTo(pos.getX(), pos.getY() - e.getStandingEyeHeight(), pos.getZ()));

            return dist <= radius || dist2 <= radius;
        }).stream();
    }

    /**
     * Gets all entities within a given range from the player.
     */
    static List<Entity> getWithinReach(PlayerEntity player, double reach, @Nullable Predicate<? super Entity> predicate) {
        Vec3d look = player.getCameraPosVec(1).multiply(reach);

        return player.world.getOtherEntities(player, player
                .getBoundingBox()
                .expand(look.x, look.y, look.z)
                .expand(1, 1, 1), predicate);
    }
}
