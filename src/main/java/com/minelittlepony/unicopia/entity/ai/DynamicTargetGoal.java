package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public class DynamicTargetGoal extends Goal {

    private final MobEntity mob;

    private int interval;

    private Optional<Entity> target;

    @Nullable
    private Predicate<Entity> test;

    public DynamicTargetGoal(MobEntity mob) {
        this.mob = mob;
    }

    public Supplier<Optional<Entity>> addPredicate(Predicate<Entity> predicate) {
        test = test == null ? predicate : predicate.or(test);

        return () -> target.filter(Entity::isAlive).filter(predicate);
    }

    public Optional<Entity> getTarget() {
        return target.filter(test).filter(Entity::isAlive);
    }

    @Override
    public boolean canStart() {
        if (interval-- <= 0) {
            interval = 20;

            target = VecHelper.findInRange(mob, mob.world, mob.getPos(), 26, test)
                .stream()
                .sorted(Comparator.comparing(e -> mob.distanceTo(e)))
                .findFirst();

            if (target.isPresent()) {
                if (target.get() instanceof LivingEntity) {
                    mob.setTarget((LivingEntity)target.get());
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void stop() {
        target = Optional.empty();
    }

    @Override
    public boolean shouldContinue() {
        LivingEntity target = mob.getTarget();
        return target != null && mob.isTarget(target, TargetPredicate.DEFAULT);
    }
}
