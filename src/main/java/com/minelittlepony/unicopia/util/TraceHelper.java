package com.minelittlepony.unicopia.util;

import java.util.*;
import java.util.function.*;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;

public class TraceHelper {
    /**
     * Performs a ray trace from the given entity and returns
     * a result for the first Entity that the ray intercepts.
     *
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     */
    public static <T extends Entity> Optional<T> findEntity(Entity e, double distance, float tickDelta, Predicate<Entity> predicate) {
        return traceEntity(e, distance, tickDelta, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(predicate)).map(Trace::toEntity);
    }

    /**
     * Performs a ray trace from the given entity and returns
     * a result for the first Entity that the ray intercepts.
     *
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     */
    public static <T extends Entity> Optional<T> findEntity(Entity e, double distance, float tickDelta) {
        return traceEntity(e, distance, tickDelta, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).map(Trace::toEntity);
    }

    /**
     * Performs a ray trace from the given entity and returns
     * a result for the first block that the ray intercepts.
     *
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     */
    public static Optional<BlockPos> findBlock(Entity e, double distance, float tickDelta) {
        return Trace.create(e, distance, tickDelta, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).getBlockPos();
    }

    /**
     * Performs a raytrace and returns all matching blocks the ray passed through.
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     * @param predicate    Predicate test to filter block states
     *
     * @return List of matching positions
     */
    public static List<BlockPos> findBlocks(Entity e, double distance, float tickDelta, Predicate<BlockState> predicate) {
        final Vec3d orientation = e.getRotationVec(tickDelta);
        final Vec3d start = e.getCameraPosVec(tickDelta);

        return BlockView.raycast(start, start.add(orientation.multiply(distance)), new ArrayList<BlockPos>(), (ctx, pos) -> {
            if (predicate.test(e.getWorld().getBlockState(pos))) {
                ctx.add(pos);
            }
            return null;
        }, ctx -> {
            return ctx;
        });
    }

    static Optional<EntityHitResult> traceEntity(Entity e, double distance, float tickDelta, Predicate<Entity> predicate) {
        final Vec3d orientation = e.getRotationVec(tickDelta);
        final Vec3d start = e.getCameraPosVec(tickDelta);

        final Box box = e.getBoundingBox().stretch(orientation.multiply(Math.abs(distance))).expand(10);

        return Optional.ofNullable(
            ProjectileUtil.raycast(e, start, start.add(orientation.multiply(distance)), box, predicate, Math.abs(distance))
        );
    }
}
