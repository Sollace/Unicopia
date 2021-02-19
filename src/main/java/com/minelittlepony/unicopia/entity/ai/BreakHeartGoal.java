package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.FloatingArtefactEntity;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class BreakHeartGoal extends Goal {

    private final MobEntity mob;

    @Nullable
    private FloatingArtefactEntity target;

    private int cooldown;

    public BreakHeartGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        Optional<FloatingArtefactEntity> item = VecHelper.findInRange(mob, mob.world, mob.getPos(), 16,
                e -> !e.removed
                && e instanceof FloatingArtefactEntity
                && ((FloatingArtefactEntity)e).getStack().getItem() == UItems.CRYSTAL_HEART
                && mob.getVisibilityCache().canSee(e)
            )
            .stream()
            .map(e -> (FloatingArtefactEntity)e)
            .sorted(Comparator.comparing((Entity e) -> mob.distanceTo(e)))
            .findFirst();

        if (item.isPresent()) {
            this.target = item.get();
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        return (target == null || (target.isAlive() && mob.squaredDistanceTo(target) <= 225)) || canStart();
    }

    @Override
    public void stop() {
        target = null;
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null || target.removed) {
            target = null;
            return;
        }

        mob.getLookControl().lookAt(target, 30, 30);

        double reach = mob.getWidth() * 2 * mob.getWidth() * 2;
        double distance = mob.squaredDistanceTo(target.getX(), target.getY(), target.getZ());

        double speed = 0.9D;

        if (distance > reach && distance < 16) {
            speed = 1.23;
        } else if (distance < 225) {
            speed = 1.6;
        }

        mob.getNavigation().startMovingTo(target, speed);

        cooldown = Math.max(this.cooldown - 1, 0);

        if (target.getY() >= mob.getY() + 2) {
            reach += 5;
        }

        if (distance <= reach && cooldown <= 0) {
            cooldown = 20;
            mob.tryAttack(target);
        }
    }
}
