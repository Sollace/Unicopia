package com.minelittlepony.unicopia.entity.ai;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;

public class PredicatedGoal extends Goal {
    private final Goal goal;
    private final BooleanSupplier predicate;

    public static void applyToAll(GoalSelector goals, BooleanSupplier predicate) {
        Set<PrioritizedGoal> existingTasks = new HashSet<>(goals.getGoals());
        goals.clear(g -> !(g instanceof PredicatedGoal));
        existingTasks.forEach(goal -> {
            if (!(goal.getGoal() instanceof PredicatedGoal)) {
                goals.add(goal.getPriority(), new PredicatedGoal(goal.getGoal(), predicate));
            }
        });
    }

    public PredicatedGoal(Goal goal, BooleanSupplier predicate) {
        this.goal = goal;
        this.predicate = predicate;
    }

    @Override
    public boolean canStart() {
        return predicate.getAsBoolean() && goal.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return predicate.getAsBoolean() && goal.shouldContinue();
    }

    @Override
    public boolean canStop() {
        return goal.canStop();
    }

    @Override
    public void start() {
        if (predicate.getAsBoolean()) {
            goal.start();
        }
    }

    @Override
    public void stop() {
        goal.stop();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return goal.shouldRunEveryTick();
    }

    @Override
    public void tick() {
        goal.tick();
    }

    @Override
    public void setControls(EnumSet<Goal.Control> controls) {
        goal.setControls(controls);
    }

    @Override
    public EnumSet<Goal.Control> getControls() {
        return goal.getControls();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else {
            return o != null && getClass() == o.getClass() ? goal.equals(((PredicatedGoal)o).goal) : false;
        }
    }

    @Override
    public int hashCode() {
        return goal.hashCode();
    }
}
