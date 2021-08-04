package com.minelittlepony.unicopia.entity.ai;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.entity.FloatingArtefactEntity;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;

public class BreakHeartGoal extends Goal {

    protected final MobEntity mob;

    private final DynamicTargetGoal targetter;
    private final Supplier<Optional<Entity>> target;

    public BreakHeartGoal(MobEntity mob, DynamicTargetGoal targetter) {
        this.mob = mob;
        this.targetter = targetter;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));

        target = targetter.addPredicate(this::canTarget);
    }

    protected boolean canTarget(Entity e) {
        return !e.isRemoved()
                && e instanceof FloatingArtefactEntity
                && ((FloatingArtefactEntity)e).getStack().getItem() == UItems.CRYSTAL_HEART
                && mob.getVisibilityCache().canSee(e);
    }

    @Override
    public boolean canStart() {
        return target.get().isPresent();
    }

    @Override
    public boolean shouldContinue() {
        return target.get().map(mob::squaredDistanceTo).orElse(0D) <= 225
                && (!mob.getNavigation().isIdle() || canStart());
    }

    @Override
    public void stop() {
        targetter.stop();
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        target.get().ifPresent(target -> {
            mob.getLookControl().lookAt(target, 30, 30);

            double reach = mob.getWidth() * 2 * mob.getWidth() * 2 * target.getWidth();
            double distance = mob.squaredDistanceTo(target.getX(), target.getY(), target.getZ());

            attackTarget(target, reach, distance);
        });
    }

    protected void attackTarget(Entity target, double reach, double distance) {
        double speed = 0.9D;

        if (distance > reach && distance < 16) {
            speed = 1.23;
        } else if (distance < 225) {
            speed = 1.6;
        } else if (distance <= 1) {
            speed = 0.5;
        }

        mob.getNavigation().startMovingTo(target, speed);

        if (target.getY() >= mob.getY() + 2) {
            reach += 5;
        }

        if (distance <= reach) {
            mob.swingHand(Hand.MAIN_HAND);
            mob.tryAttack(target);
        }
    }
}
