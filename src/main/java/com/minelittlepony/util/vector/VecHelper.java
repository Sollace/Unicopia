package com.minelittlepony.util.vector;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VecHelper {

    /**
     * Performs a ray cast from the given entity and returns a result for the first block that ray intercepts.
     *
     * @param e                Entity to start from
     * @param distance        Maximum distance
     * @param partialTick    Client partial ticks
     *
     * @return    RayTraceResult result or null
     */
    public static RayTraceResult rayTrace(Entity e, double distance, float partialTicks) {
        Vec3d pos = e.getPositionEyes(partialTicks);
        Vec3d look = e.getLook(partialTicks).scale(distance);

        return e.world.rayTraceBlocks(pos, pos.add(look), false, false, true);
    }

    /**
     * Gets the entity the player is currently looking at, or null.
     */
    @Nullable
    public static Entity getLookedAtEntity(EntityLivingBase e, int reach) {
        RayTraceResult objectMouseOver = getObjectMouseOver(e, reach, 1);

        if (objectMouseOver != null && objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY) {
            return objectMouseOver.entityHit;
        }

        return null;
    }

    public static Stream<Entity> findAllEntitiesInRange(@Nullable Entity origin, World w, BlockPos pos, double radius) {

        BlockPos begin = pos.add(-radius, -radius, -radius);
        BlockPos end = pos.add(radius, radius, radius);

        AxisAlignedBB bb = new AxisAlignedBB(begin, end);

        return w.getEntitiesInAABBexcluding(origin, bb, null).stream().filter(e -> {
            double dist = e.getDistance(pos.getX(), pos.getY(), pos.getZ());
            double dist2 = e.getDistance(pos.getX(), pos.getY() - e.getEyeHeight(), pos.getZ());

            return dist <= radius || dist2 <= radius;
        });
    }

    /**
     * Gets all entities within a given range from the player.
     */
    public static List<Entity> getWithinRange(EntityPlayer player, double reach, @Nullable Predicate<? super Entity> predicate) {
        Vec3d look = player.getLook(0).scale(reach);

        return player.world.getEntitiesInAABBexcluding(player, player
                .getEntityBoundingBox()
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
    public static RayTraceResult getObjectMouseOver(Entity e, double distance, float partialTick) {
        return getObjectMouseOver(e, distance, partialTick, EntitySelectors.NOT_SPECTATING);
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
    public static RayTraceResult getObjectMouseOver(Entity e, double distance, float partialTick, Predicate<Entity> predicate) {
        RayTraceResult tracedBlock = rayTrace(e, distance, partialTick);

        double totalTraceDistance = distance;

        Vec3d pos = e.getPositionEyes(partialTick);

        if (tracedBlock != null) {
            totalTraceDistance = tracedBlock.hitVec.distanceTo(pos);
        }

        Vec3d look = e.getLook(partialTick);
        Vec3d ray = pos.add(look.scale(distance));

        Vec3d hit = null;
        Entity pointedEntity = null;
        List<Entity> entitiesWithinRange = e.world.getEntitiesInAABBexcluding(e, e.getEntityBoundingBox()
                .grow(look.x * distance, look.y * distance, look.z * distance)
                .expand(1, 1, 1), predicate);

        double traceDistance = totalTraceDistance;

        for (Entity entity : entitiesWithinRange) {
            if (entity.canBeCollidedWith()) {
                double size = entity.getCollisionBorderSize();
                AxisAlignedBB entityAABB = entity.getEntityBoundingBox().expand(size, size, size);
                RayTraceResult intercept = entityAABB.calculateIntercept(pos, ray);

                if (entityAABB.contains(pos)) {
                    if (0 < traceDistance || traceDistance == 0) {
                        pointedEntity = entity;
                        hit = intercept == null ? pos : intercept.hitVec;
                        traceDistance = 0;
                    }
                } else if (intercept != null) {
                    double distanceToHit = pos.distanceTo(intercept.hitVec);
                    if (distanceToHit < traceDistance || traceDistance == 0) {
                        if (entity == e.getRidingEntity()) {
                            if (traceDistance == 0) {
                                pointedEntity = entity;
                                hit = intercept.hitVec;
                            }
                        } else {
                            pointedEntity = entity;
                            hit = intercept.hitVec;
                            traceDistance = distanceToHit;
                        }
                    }
                }
            }
        }

        if (pointedEntity != null && (traceDistance < totalTraceDistance || tracedBlock == null)) {
            return new RayTraceResult(pointedEntity, hit);
        }

        return tracedBlock;
    }
}
