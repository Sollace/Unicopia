package com.minelittlepony.unicopia.entity.ai;

import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EatMuffinGoal extends BreakHeartGoal {

    private int timer;

    private boolean eatingStarted;

    public EatMuffinGoal(MobEntity mob, DynamicTargetGoal targetter) {
        super(mob, targetter);
    }

    public int getTimer() {
        return eatingStarted ? timer : 0;
    }

    @Override
    public void start() {
        eatingStarted = false;
        timer = getTickCount(10);
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        super.stop();
        eatingStarted = false;
        timer = 0;
    }

    @Override
    public boolean shouldContinue() {
        return timer > 0 && super.shouldContinue();
    }

    @Override
    protected boolean canTarget(Entity e) {
        return !e.isRemoved()
                && e instanceof PhysicsBodyProjectileEntity
                && ((PhysicsBodyProjectileEntity)e).getStack().getItem() == UItems.MUFFIN
                && mob.getVisibilityCache().canSee(e);
    }

    @Override
    protected void attackTarget(Entity target, double reach, double distance) {
        double speed = 1D;

        if (distance > 5) {
            speed += 0.5;
        }
        if (distance < 2) {
            speed += 0.1D;
        }

        mob.getNavigation().startMovingTo(target, speed);

        if (distance <= reach + 0.5) {
            eatingStarted = true;

            if (target instanceof PhysicsBodyProjectileEntity projectile) {
                mob.eatFood(mob.getWorld(), projectile.getStack());
                projectile.discard();

                if (mob instanceof AnimalEntity animal) {
                    if (mob.getWorld().random.nextInt(12) == 0) {
                        Entity player = ((PhysicsBodyProjectileEntity) target).getOwner();

                        animal.lovePlayer(player instanceof PlayerEntity ? (PlayerEntity)player : null);
                        animal.setLoveTicks(1000);
                    }
                }
            }
        }
    }
}
