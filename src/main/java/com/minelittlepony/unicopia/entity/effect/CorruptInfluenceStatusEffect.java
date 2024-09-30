package com.minelittlepony.unicopia.entity.effect;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.damage.UDamageTypes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.WorldEvents;

public class CorruptInfluenceStatusEffect extends SimpleStatusEffect {
    static final Identifier CORRUPTION_MODIFIER_ID = Unicopia.id("corruption_modifier");

    CorruptInfluenceStatusEffect(int color) {
        super(StatusEffectCategory.NEUTRAL, color, false);
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, CORRUPTION_MODIFIER_ID, 15, EntityAttributeModifier.Operation.ADD_VALUE);
        addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, CORRUPTION_MODIFIER_ID, 10, EntityAttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {

        if (entity.getWorld().isClient) {
            return true;
        }

        if (entity instanceof HostileEntity mob) {

            int nearby = entity.getWorld().getOtherEntities(entity, entity.getBoundingBox().expand(40), i -> i.getType() == entity.getType()).size();

            if (nearby > 1) {
                if (Equine.of(entity).filter(eq -> eq instanceof Owned<?> o && o.getMaster() != null).isPresent()) {
                    return false;
                }

                if (entity.getWorld().random.nextInt(2000) != 0) {
                    return true;
                }
            } else if (entity.getWorld().random.nextInt(200) != 0) {
                return true;
            }

            reproduce(mob);


        } else if (entity.age % 2000 == 0) {
            entity.damage(Living.living(entity).damageOf(UDamageTypes.ALICORN_AMULET), 2);
        }

        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration > 0;
    }

    public static void reproduce(HostileEntity mob) {
        HostileEntity clone = (HostileEntity)mob.getType().create(mob.getWorld());
        clone.copyPositionAndRotation(mob);
        clone.takeKnockback(0.1, 0.5, 0.5);
        mob.takeKnockback(0.1, -0.5, -0.5);
        if (mob.getRandom().nextInt(4) != 0) {
            mob.clearStatusEffects();
        } else {
            if (clone.getAttributes().hasAttribute(EntityAttributes.GENERIC_MAX_HEALTH)) {
                float maxHealthDifference = mob.getMaxHealth() - clone.getMaxHealth();
                clone.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 900000, 2));
                clone.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                    .addPersistentModifier(new EntityAttributeModifier(CORRUPTION_MODIFIER_ID, maxHealthDifference + 1, EntityAttributeModifier.Operation.ADD_VALUE));
            }
            if (clone.getAttributes().hasAttribute(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
                clone.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    .addPersistentModifier(new EntityAttributeModifier(CORRUPTION_MODIFIER_ID, mob.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + 1, EntityAttributeModifier.Operation.ADD_VALUE));
            }
        }

        if (mob.getRandom().nextInt(4) != 0) {
            clone.clearStatusEffects();
        }
        mob.getWorld().spawnEntity(clone);

        if (!mob.isSilent()) {
            mob.getWorld().syncWorldEvent((PlayerEntity)null, WorldEvents.ZOMBIE_INFECTS_VILLAGER, mob.getBlockPos(), 0);
        }
    }
}
