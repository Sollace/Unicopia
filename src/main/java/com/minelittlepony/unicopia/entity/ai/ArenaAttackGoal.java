package com.minelittlepony.unicopia.entity.ai;

import com.minelittlepony.unicopia.entity.ArenaCombatant;

import net.minecraft.entity.ai.goal.AttackGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

public class ArenaAttackGoal<E extends MobEntity & ArenaCombatant> extends AttackGoal {

    private final E combatant;

    public ArenaAttackGoal(E mob) {
        super(mob);
        this.combatant = mob;
    }

    @Override
    public boolean canStart() {
        return combatant.getHomePos().isPresent() && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return combatant.getHomePos().filter(this::isInArena).isPresent() && super.shouldContinue();
    }

    private boolean isInArena(BlockPos homePos) {
        return combatant.getBlockPos().isWithinDistance(homePos, combatant.getAreaRadius());
    }

    @Override
    public void stop() {
        super.stop();
        combatant.setTarget(null);
        combatant.getHomePos().ifPresent(home -> {

            Path path = combatant.getNavigation().findPathTo(home, 2, (int)combatant.getAreaRadius() * 2);
            if (path != null) {
                combatant.getNavigation().startMovingAlong(path, combatant.getMovementSpeed() * 2F);
            } else {
                combatant.teleportTo(home.toCenterPos());
            }
        });
    }
}
