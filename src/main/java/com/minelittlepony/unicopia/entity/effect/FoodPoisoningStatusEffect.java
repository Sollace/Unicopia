package com.minelittlepony.unicopia.entity.effect;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.effect.StatusEffects;

public class FoodPoisoningStatusEffect extends StatusEffect {

    FoodPoisoningStatusEffect(int color) {
        super(StatusEffectType.HARMFUL, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {

        StatusEffectInstance nausea = entity.getStatusEffect(StatusEffects.NAUSEA);
        if (nausea == null) {
            StatusEffectInstance foodEffect = entity.getStatusEffect(this);
            nausea = new StatusEffectInstance(StatusEffects.NAUSEA, foodEffect.getDuration(),
                    foodEffect.getAmplifier(),
                    true,
                    foodEffect.shouldShowParticles(),
                    false
            );

            entity.addStatusEffect(nausea);
        }

        entity.damage(MagicalDamageSource.FOOD_POISONING, amplifier);

    }

    @Override
    public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        applyUpdateEffect(target, amplifier);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int i = 40 >> amplifier;
        return i <= 0 || duration % i == 0;
    }
}
