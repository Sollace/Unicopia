package com.minelittlepony.unicopia.entity.ai;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;

public class WantItTakeItGoal extends Goal {

    private final MobEntity mob;

    @Nullable
    private LivingEntity target;
    @Nullable
    private ItemEntity item;

    private int cooldown;

    public WantItTakeItGoal(MobEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = mob.getTarget();
        if (target == null || !WantItNeedItTargetGoal.canTarget(target)) {

            Optional<ItemEntity> item = VecHelper.findInRange(mob, mob.world, mob.getPos(), 16,
                    e -> !e.removed && e instanceof ItemEntity && EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, ((ItemEntity)e).getStack()) > 0)
                .stream()
                .map(e -> (ItemEntity)e)
                .sorted(Comparator.comparing((Entity e) -> mob.distanceTo(e)))
                .findFirst();

            if (item.isPresent()) {
                this.item = item.get();
                return true;
            }

            return false;
        }

        this.target = target;
        return true;
    }

    @Override
    public boolean shouldContinue() {
        return (target == null || (
                target.isAlive()
                && WantItNeedItTargetGoal.canTarget(target)
                && mob.squaredDistanceTo(target) <= 225))
                && (item == null || item.isAlive())
                && (!mob.getNavigation().isIdle() || canStart());
    }

    @Override
    public void stop() {
        target = null;
        item = null;
        mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (target == null && (item == null || item.removed)) {
            return;
        }

        Entity targetEntity = target == null ? item : target;

        mob.getLookControl().lookAt(targetEntity, 30, 30);

        double reach = mob.getWidth() * 2 * mob.getWidth() * 2;
        double distance = mob.squaredDistanceTo(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());

        double speed = 0.8D;

        if (distance > reach && distance < 16) {
            speed = 1.33;
        } else if (distance < 225) {
            speed = 0.6;
        }
        speed *= 2;

        ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, mob, 0.2F), mob, 1);

        mob.getNavigation().startMovingTo(targetEntity, speed);

        cooldown = Math.max(this.cooldown - 1, 0);
        if (distance <= reach) {
            if (target != null) {
                if (cooldown <= 0) {
                    cooldown = 20;
                    mob.tryAttack(target);

                    if (mob.world.random.nextInt(20) == 0) {
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            ItemStack stack = target.getEquippedStack(slot);
                            if (EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, stack) > 0) {
                                target.equipStack(slot, ItemStack.EMPTY);
                                AwaitTickQueue.scheduleTask(mob.world, w -> {
                                    mob.tryEquip(stack);
                                }, 0);
                            }
                        }
                    }
                }
            } else {
                ItemStack stack = item.getStack();
                AwaitTickQueue.scheduleTask(mob.world, w -> {
                    if (!item.removed) {
                        mob.tryEquip(stack);
                        mob.method_29499(item);
                        mob.sendPickup(item, stack.getCount());
                        item.remove();
                    }
                }, 0);
            }
        }
    }
}
