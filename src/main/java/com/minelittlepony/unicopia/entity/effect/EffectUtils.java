package com.minelittlepony.unicopia.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

public interface EffectUtils {
    static boolean isPoisoned(LivingEntity entity) {
        return getAmplifier(entity, UEffects.FOOD_POISONING) > 2;
    }

    static int getAmplifier(LivingEntity entity, StatusEffect effect) {
        return entity.hasStatusEffect(effect) ? entity.getStatusEffect(effect).getAmplifier() : 0;
    }

    static boolean isChangingRace(LivingEntity entity) {
        return entity.getStatusEffects().stream().anyMatch(effect -> effect.getEffectType() instanceof RaceChangeStatusEffect);
    }

    static boolean hasExtraDefenses(LivingEntity entity) {
        return entity.hasStatusEffect(UEffects.FORTIFICATION);
    }

    static boolean applyStatusEffect(LivingEntity entity, StatusEffect effect, boolean apply) {
        if (entity.getWorld().isClient) {
            return false;
        }
        boolean has = entity.hasStatusEffect(effect);
        if (has != apply) {
            if (has) {
                if (entity.getStatusEffect(effect).getDuration() == StatusEffectInstance.INFINITE) {
                    entity.removeStatusEffect(effect);
                }
            } else {
                entity.addStatusEffect(new StatusEffectInstance(effect, StatusEffectInstance.INFINITE, 0, false, false, true));
            }
            return true;
        }
        return false;
    }
}
