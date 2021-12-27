package com.minelittlepony.unicopia.entity.ai;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class WantItTakeItGoal extends BreakHeartGoal {

    private final TargetPredicate predicate = TargetPredicate.createAttackable()
            .setBaseMaxDistance(64)
            .setPredicate(EquinePredicates.HAS_WANT_IT_NEED_IT);

    protected int cooldown;

    public WantItTakeItGoal(MobEntity mob, DynamicTargetGoal targetter) {
        super(mob, targetter);
    }

    @Override
    protected boolean canTarget(Entity e) {
        return (!e.isRemoved() && e instanceof ItemEntity && EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, ((ItemEntity)e).getStack()) > 0)
            || (e instanceof LivingEntity && predicate.test(mob, (LivingEntity)e));
    }

    @Override
    protected void attackTarget(Entity target, double reach, double distance) {
        ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, mob, 0.2F), mob, 1);

        double speed = 0.8D;

        if (distance > reach && distance < 16) {
            speed = 1.33;
        } else if (distance < 225) {
            speed = 0.6;
        }
        if (Math.abs(reach - distance) > 1) {
            speed *= 2;
        }

        mob.getNavigation().startMovingTo(target, speed);

        cooldown = Math.max(cooldown - 1, 0);

        if (distance <= reach) {
            if (target instanceof LivingEntity) {
                if (cooldown <= 0) {
                    cooldown = 20;
                    mob.tryAttack(target);
                    mob.swingHand(Hand.MAIN_HAND);

                    if (mob.world.random.nextInt(20) == 0) {

                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            ItemStack stack = ((LivingEntity)target).getEquippedStack(slot);
                            if (EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, stack) > 0) {
                                target.equipStack(slot, ItemStack.EMPTY);
                                AwaitTickQueue.scheduleTask(mob.world, w -> mob.tryEquip(stack));
                            }
                        }
                    }
                }
            } else if (target instanceof ItemEntity) {
                AwaitTickQueue.scheduleTask(mob.world, w -> {
                    ItemEntity item = (ItemEntity)target;
                    ItemStack stack = item.getStack();

                    if (!item.isRemoved()) {
                        mob.tryEquip(stack);
                        mob.triggerItemPickedUpByEntityCriteria(item);
                        mob.sendPickup(item, stack.getCount());
                        item.remove(RemovalReason.DISCARDED);
                    }
                });
            }
        }
    }
}
