package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.util.math.Vec3d;

public class FleeExplosionGoal extends Goal {
    private static final Predicate<Entity> SOURCE_PREDICATE = e -> e instanceof TntMinecartEntity || e instanceof TntEntity;

    private final PathAwareEntity mob;
    private final double slowSpeed;
    private final double fastSpeed;
    private final Comparator<Entity> sorting;

    @Nullable
    private Entity targetEntity;
    @Nullable
    private Path fleePath;

    public FleeExplosionGoal(PathAwareEntity mob, float distance, double slowSpeed, double fastSpeed) {
        this.setControls(EnumSet.of(Goal.Control.MOVE));
        this.mob = mob;
        this.slowSpeed = slowSpeed;
        this.fastSpeed = fastSpeed;
        this.sorting = Comparator.comparingDouble(e -> e.squaredDistanceTo(mob));
    }

    public void setFleeTarget(@Nullable Entity target) {
        this.targetEntity = target;
    }

    @Override
    public boolean canStart() {
        if (targetEntity == null || targetEntity.isRemoved()) {
            targetEntity = mob.getWorld().getOtherEntities(mob, mob.getBoundingBox().expand(5, 3, 5), SOURCE_PREDICATE).stream().sorted(sorting).findFirst().orElse(null);
        }

        if (targetEntity == null) {
            return false;
        }

        Vec3d targetPosition = NoPenaltyTargeting.findFrom(mob, 16, 7, targetEntity.getPos());
        if (targetPosition == null
                || targetEntity.squaredDistanceTo(targetPosition.x, targetPosition.y, targetPosition.z) < targetEntity.squaredDistanceTo(mob)) {
            return false;
        }
        fleePath = mob.getNavigation().findPathTo(targetPosition.x, targetPosition.y, targetPosition.z, 0);
        return fleePath != null;
    }

    @Override
    public boolean shouldContinue() {
        return !mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        mob.getNavigation().startMovingAlong(fleePath, slowSpeed);
    }

    @Override
    public void stop() {
        targetEntity = null;
    }

    @Override
    public void tick() {
        if (mob.squaredDistanceTo(targetEntity) < 49.0) {
            mob.getNavigation().setSpeed(fastSpeed);
        } else {
            mob.getNavigation().setSpeed(slowSpeed);
        }
    }

    public static void notifySurroundings(Entity explosionSource, float radius) {
        explosionSource.getWorld().getOtherEntities(explosionSource, explosionSource.getBoundingBox().expand(radius), e -> {
           return Living.getOrEmpty(e).filter(l -> l instanceof Creature c).isPresent();
        }).forEach(e -> {
            getGoals((Creature)Living.living(e)).forEach(goal -> goal.setFleeTarget(explosionSource));
        });
    }

    private static Stream<FleeExplosionGoal> getGoals(Creature creature) {
        return creature.getGoals().stream()
                .flatMap(goals -> goals.getGoals().stream())
                .map(PrioritizedGoal::getGoal)
                .filter(g -> g instanceof FleeExplosionGoal)
                .map(FleeExplosionGoal.class::cast);
    }
}