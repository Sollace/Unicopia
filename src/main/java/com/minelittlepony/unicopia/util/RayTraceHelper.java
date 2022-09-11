package com.minelittlepony.unicopia.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RayTraceHelper {
    public static <T extends Entity> Optional<T> findEntity(Entity e, double distance, float tickDelta, Predicate<Entity> predicate) {
        return doTrace(e, distance, tickDelta, EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(predicate)).getEntity();
    }

    /**
     * Performs a ray trace from the given entity and returns
     * a result for the first Entity or block that the ray intercepts.
     *
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     *
     * @return A Trace describing what was found.
     */
    public static Trace doTrace(Entity e, double distance, float tickDelta) {
        return doTrace(e, distance, tickDelta, EntityPredicates.EXCEPT_SPECTATOR);
    }

    /**
     * Performs a ray trace from the given entity and returns
     * a result for the first Entity that passes the given predicate
     * or block that the ray intercepts.
     *
     *
     * @param e            Entity to start from
     * @param distance     Maximum distance
     * @param tickDelta    Client partial ticks
     * @param predicate    Predicate test to filter entities
     *
     * @return A Trace describing what was found.
     */
    public static Trace doTrace(Entity e, double distance, float tickDelta, Predicate<Entity> predicate) {
        final Vec3d ray = e.getRotationVec(tickDelta).multiply(distance);
        final Vec3d start = e.getCameraPosVec(tickDelta);

        final Box box = e.getBoundingBox().stretch(ray).expand(1);

        EntityHitResult pointedEntity = ProjectileUtil.raycast(e, start, start.add(ray), box, predicate, distance);

        if (pointedEntity != null) {
            return new Trace(pointedEntity);
        }

        return new Trace(e.raycast(distance, tickDelta, false));
    }

    public static class Trace {
        @Nullable
        private final HitResult result;

        Trace(@Nullable HitResult result) {
            this.result = result;
        }

        @Nullable
        public HitResult getResult() {
            return result;
        }

        @SuppressWarnings("unchecked")
        public <T extends Entity> Optional<T> getEntity() {
            if (result != null && result.getType() == HitResult.Type.ENTITY) {
                return Optional.of((T)((EntityHitResult)result).getEntity());
            }
            return Optional.empty();
        }

        public Optional<BlockPos> getBlockPos() {
            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                return Optional.of(((BlockHitResult)result).getBlockPos());
            }
            return Optional.empty();
        }

        public Trace ifEntity(Consumer<Entity> consumer) {
            getEntity().ifPresent(consumer);
            return this;
        }

        public Trace ifBlock(Consumer<BlockPos> consumer) {
            getBlockPos().ifPresent(consumer);
            return this;
        }
    }
}
