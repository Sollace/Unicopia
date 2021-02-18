package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.Optional;

import com.minelittlepony.unicopia.EquinePredicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class WantItNeedItTargetGoal extends Goal {

    private final TargetPredicate predicate;

    private final MobEntity mob;

    private int interval;

    public WantItNeedItTargetGoal(MobEntity mob) {
        this.predicate = new TargetPredicate()
                .setBaseMaxDistance(64)
                .setPredicate(EquinePredicates.HAS_WANT_IT_NEED_IT);
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        if (interval-- <= 0) {
            interval = 20;
            Optional<LivingEntity> target = mob.world.getOtherEntities(mob, mob.getBoundingBox().expand(16, 16, 16),
                    e -> e instanceof LivingEntity && predicate.test(mob, (LivingEntity)e))
                .stream()
                .map(e -> (LivingEntity)e)
                .sorted(Comparator.comparing((Entity e) -> mob.distanceTo(e)))
                .findFirst();

            if (target.isPresent()) {
                mob.setTarget(target.get());
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = mob.getTarget();
        return target != null && mob.isTarget(target, TargetPredicate.DEFAULT);
    }
}
