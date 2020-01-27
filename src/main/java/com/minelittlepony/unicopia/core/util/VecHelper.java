package com.minelittlepony.unicopia.core.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

public class VecHelper {

    /**
     * Performs a ray cast from the given entity and returns a result for the first block that ray intercepts.
     *
     * Server-available version of Entity.rayTrace
     *
     * @param e                Entity to start from
     * @param distance        Maximum distance
     * @param partialTick    Client partial ticks
     *
     * @return    RayTraceResult result or null
     */
    public static HitResult rayTrace(Entity e, double distance, float partialTick) {
        Vec3d cam = e.getCameraPosVec(partialTick);
        Vec3d look = e.getRotationVec(partialTick).multiply(distance);

        return e.world.rayTrace(
            new RayTraceContext(cam, cam.add(look),
                RayTraceContext.ShapeType.OUTLINE,
                RayTraceContext.FluidHandling.NONE,
            e)
        );
    }

    /**
     * Gets the entity the player is currently looking at, or null.
     */
    @Nullable
    public static Entity getLookedAtEntity(LivingEntity e, int reach) {
        HitResult objectMouseOver = getObjectMouseOver(e, reach, 1);

        if (objectMouseOver instanceof EntityHitResult && objectMouseOver.getType() == BlockHitResult.Type.ENTITY) {
            return ((EntityHitResult)objectMouseOver).getEntity();
        }

        return null;
    }

    public static Stream<Entity> findAllEntitiesInRange(@Nullable Entity origin, World w, BlockPos pos, double radius) {

        BlockPos begin = pos.add(-radius, -radius, -radius);
        BlockPos end = pos.add(radius, radius, radius);

        Box bb = new Box(begin, end);

        return w.getEntities(origin, bb, null).stream().filter(e -> {
            double dist = e.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
            double dist2 = e.squaredDistanceTo(pos.getX(), pos.getY() - e.getStandingEyeHeight(), pos.getZ());

            return dist <= radius || dist2 <= radius;
        });
    }

    /**
     * Gets all entities within a given range from the player.
     */
    public static List<Entity> getWithinRange(PlayerEntity player, double reach, @Nullable Predicate<? super Entity> predicate) {
        Vec3d look = player.getCameraPosVec(1).multiply(reach);

        return player.world.getEntities(player, player
                .getBoundingBox()
                .expand(look.x, look.y, look.z)
                .expand(1, 1, 1), predicate);
    }

    /**
     * Performs a ray trace from the given entity and returns a result for the first Entity or block that the ray intercepts.
     *
     * @param e                Entity to start from
     * @param distance        Maximum distance
     * @param partialTick    Client partial ticks
     *
     * @return    RayTraceResult result or null
     */
    public static HitResult getObjectMouseOver(Entity e, double distance, float partialTick) {
        return getObjectMouseOver(e, distance, partialTick, EntityPredicates.EXCEPT_SPECTATOR);
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
     * @return    RayTraceResult result or null
     */
    public static HitResult getObjectMouseOver(Entity e, double distance, float partialTick, Predicate<Entity> predicate) {
        HitResult tracedBlock = rayTrace(e, distance, partialTick);

        double totalTraceDistance = distance;

        Vec3d pos = e.getCameraPosVec(partialTick);

        if (tracedBlock != null) {
            totalTraceDistance = tracedBlock.getPos().distanceTo(pos);
        }

        Vec3d look = e.getRotationVec(partialTick);
        Vec3d ray = pos.add(look.multiply(distance));

        Vec3d hit = null;
        Entity pointedEntity = null;
        List<Entity> entitiesWithinRange = e.world.getEntities(e, e.getBoundingBox()
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
            return new EntityHitResult(pointedEntity, hit);
        }

        return tracedBlock;
    }
}
