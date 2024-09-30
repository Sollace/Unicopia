package com.minelittlepony.unicopia.entity.ai;

import com.minelittlepony.unicopia.AwaitTickQueue;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.item.enchantment.WantItNeedItEnchantment;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Hand;

public class WantItTakeItGoal extends BreakHeartGoal {

    private final TargetPredicate predicate = TargetPredicate.createNonAttackable()
            .setBaseMaxDistance(64)
            .setPredicate(EquinePredicates.LIVING_HAS_WANT_IT_NEED_IT.and(LivingEntity::canTakeDamage).and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR));

    protected int cooldown;

    private final Creature creature;

    public WantItTakeItGoal(Creature creature, DynamicTargetGoal targetter) {
        super((MobEntity)creature.asEntity(), targetter);
        this.creature = creature;
    }

    @Override
    protected boolean canTarget(Entity e) {
        return e != null && !e.isRemoved() && (
                  (e instanceof LivingEntity l && predicate.test(mob, l)
               || (e instanceof ItemEntity i && WantItNeedItEnchantment.getLevel(i) > 0)
            )
        );
    }

    @Override
    protected void attackTarget(Entity target, double reach, double distance) {
        ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, mob.getPos().add(VecHelper.sphere(mob.getWorld().random).get()), 0.2F), mob, 1);

        double speed = 0.8D;

        if (distance > reach && distance < 16) {
            speed = 1.33;
        } else if (distance < 225) {
            speed = 0.6;
        }
        if (Math.abs(reach - distance) > 1) {
            speed *= 2;
        }

        reach = Math.max(1.5, reach);

        mob.getNavigation().startMovingTo(target, speed);

        cooldown = Math.max(cooldown - 1, 0);
        creature.setSmitten(true);

        if (distance <= reach) {
            if (target instanceof LivingEntity living) {
                if (cooldown <= 0) {
                    cooldown = 20;
                    mob.tryAttack(target);
                    mob.swingHand(Hand.MAIN_HAND);

                    if (mob.getWorld().random.nextInt(20) == 0) {
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            ItemStack stack = living.getEquippedStack(slot);
                            if (EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, stack) > 0) {
                                AwaitTickQueue.scheduleTask(mob.getWorld(), w -> {
                                    living.equipStack(slot, ItemStack.EMPTY);
                                    mob.tryEquip(stack);
                                });
                                break;
                            }
                        }
                    }
                }
            } else if (target instanceof ItemEntity) {
                AwaitTickQueue.scheduleTask(mob.getWorld(), w -> {
                    ItemEntity item = (ItemEntity)target;
                    ItemStack stack = item.getStack();

                    if (!item.isRemoved()) {
                        ItemStack collected = mob.tryEquip(stack.copy());
                        if (!collected.isEmpty()) {
                            mob.triggerItemPickedUpByEntityCriteria(item);
                            mob.sendPickup(item, stack.getCount());
                            stack.decrement(collected.getCount());
                            if (stack.isEmpty()) {
                                item.discard();
                            }
                        }
                    }
                });
            }
        }
    }
}
