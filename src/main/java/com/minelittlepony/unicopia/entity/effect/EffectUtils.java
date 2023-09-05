package com.minelittlepony.unicopia.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;

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
}
