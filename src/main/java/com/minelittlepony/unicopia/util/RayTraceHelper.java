package com.minelittlepony.unicopia.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;

public class RayTraceHelper {
    public static <T extends Entity> Optional<T> findEntity(Entity e, double distance) {
        return doTrace(e, distance, 1).getEntity();
    }

    public static <T extends Entity> Optional<T> findEntity(Entity e, double distance, float partialTick, Predicate<Entity> predicate) {
        return doTrace(e, distance, partialTick, EntityPredicates.EXCEPT_SPECTATOR.and(predicate)).getEntity();
    }

    public static Trace doTrace(Entity e, double distance, float partialTick) {
        return doTrace(e, distance, partialTick, EntityPredicates.EXCEPT_SPECTATOR);
    }

    /**
     * Performs a ray trace from the given entity and returns a result for the first Entity that passing the given predicate or block that the ray intercepts.
     * <p>
     *
     *
     * @param e                Entity to start from
     * @param distance        Maximum distance
     * @param partialTick    Client partial ticks
     * @param predicate        Predicate test to filter entities
     *
     * @return A OptionalHit describing what was found.
     */
    public static Trace doTrace(Entity e, double distance, float partialTick, Predicate<Entity> predicate) {
        HitResult tracedBlock = traceBlocks(e, distance, partialTick, false);

        double totalTraceDistance = distance;

        Vec3d pos = e.getCameraPosVec(partialTick);

        if (tracedBlock != null) {
            totalTraceDistance = tracedBlock.getPos().distanceTo(pos);
        }

        Vec3d look = e.getRotationVec(partialTick);
        Vec3d ray = pos.add(look.multiply(distance));

        Vec3d hit = null;
        Entity pointedEntity = null;
        List<Entity> entitiesWithinRange = e.world.getOtherEntities(e, e.getBoundingBox()
                .expand(look.x * distance, look.y * distance, look.z * distance)
                .expand(1, 1, 1), predicate);

        double traceDistance = totalTraceDistance;

        for (Entity entity : entitiesWithinRange) {
            if (entity.collides()) {
                Box entityAABB = entity.getBoundingBox().expand(entity.getTargetingMargin());

                Optional<Vec3d> intercept = entityAABB.rayTrace(pos, ray);

                if (entityAABB.contains(pos)) {
                    if (traceDistance <= 0) {
                        pointedEntity = entity;
                        hit = intercept.orElse(null);
                        traceDistance = 0;
                    }
                } else if (intercept.isPresent()) {
                    Vec3d inter = intercept.get();
                    double distanceToHit = pos.distanceTo(inter);

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
        }

        if (pointedEntity != null && (traceDistance < totalTraceDistance || tracedBlock == null)) {
            return new Trace(new EntityHitResult(pointedEntity, hit));
        }

        return new Trace(tracedBlock);
    }

    /**
     * Server-available version of Entity.rayTrace
     */
    public static HitResult traceBlocks(Entity e, double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3d start = e.getCameraPosVec(tickDelta);
        Vec3d end = e.getRotationVec(tickDelta).multiply(maxDistance).add(start);

        return e.world.rayTrace(new RayTraceContext(start, end,
                RayTraceContext.ShapeType.OUTLINE,
                includeFluids ? RayTraceContext.FluidHandling.ANY : RayTraceContext.FluidHandling.NONE,
                e)
        );
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
