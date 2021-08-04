package com.minelittlepony.unicopia.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RayTraceHelper {
    public static <T extends Entity> Optional<T> findEntity(Entity e, double distance, float tickDelta, Predicate<Entity> predicate) {
        return doTrace(e, distance, tickDelta, EntityPredicates.EXCEPT_SPECTATOR.and(predicate)).getEntity();
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
        HitResult tracedBlock = e.raycast(distance, tickDelta, false);

        final Vec3d start = e.getCameraPosVec(tickDelta);

        final double totalTraceDistance = tracedBlock == null ? distance : tracedBlock.getPos().distanceTo(start);

        final Vec3d ray = e.getRotationVec(tickDelta).multiply(distance);
        final Vec3d end = start.add(ray);

        Vec3d hit = null;
        Entity pointedEntity = null;

        double traceDistance = totalTraceDistance;

        for (Entity entity : e.world.getOtherEntities(e,
                e.getBoundingBox().expand(ray.x + 1, ray.y + 1, ray.z + 1),
                predicate.and(Entity::collides)
        )) {
            Box entityAABB = entity.getBoundingBox().expand(entity.getTargetingMargin());

            Optional<Vec3d> intercept = entityAABB.raycast(start, end);

            if (entityAABB.contains(start)) {
                if (traceDistance <= 0) {
                    pointedEntity = entity;
                    hit = intercept.orElse(null);
                    traceDistance = 0;
                }
            } else if (intercept.isPresent()) {
                Vec3d inter = intercept.get();
                double distanceToHit = start.distanceTo(inter);

                if (distanceToHit < traceDistance || traceDistance == 0) {
                    if (entity.getRootVehicle() == e.getRootVehicle()) {
                        if (traceDistance == 0) {
                            pointedEntity = entity;
                            hit = inter;
                        }
                    } else {
                        pointedEntity = entity;
                        hit = inter;
                        traceDistance = distanceToHit;
                    }
                }
            }
        }

        if (pointedEntity != null && (traceDistance < totalTraceDistance || tracedBlock == null)) {
            return new Trace(new EntityHitResult(pointedEntity, hit));
        }

        return new Trace(tracedBlock);
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
