package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;

public class PrioritizedActiveTargetGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {

    private final Comparator<T> prioritySorting;

    public PrioritizedActiveTargetGoal(MobEntity mob, Class<T> targetClass, Comparator<T> prioritySorting, boolean checkVisibility) {
        super(mob, targetClass, checkVisibility);
        this.prioritySorting = prioritySorting;
    }

    public PrioritizedActiveTargetGoal(MobEntity mob, Class<T> targetClass, Comparator<T> prioritySorting, boolean checkVisibility, Predicate<LivingEntity> targetPredicate) {
        super(mob, targetClass, 10, checkVisibility, false, targetPredicate);
        this.prioritySorting = prioritySorting;
    }

    public PrioritizedActiveTargetGoal(MobEntity mob, Class<T> targetClass, Comparator<T> prioritySorting, boolean checkVisibility, boolean checkCanNavigate) {
        super(mob, targetClass, 10, checkVisibility, checkCanNavigate, null);
        this.prioritySorting = prioritySorting;
    }

    @Override
    protected void findClosestTarget() {
        targetEntity = TargettingUtil.getTargets(targetClass, targetPredicate, mob, getSearchBox(getFollowRange()))
                .sorted(prioritySorting.thenComparing(TargettingUtil.nearestTo(mob)))
                .findFirst()
                .orElse(null);
    }
}
