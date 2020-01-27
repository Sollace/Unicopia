package com.minelittlepony.unicopia.core;

import com.minelittlepony.unicopia.core.util.CustomStatusEffect;
import com.minelittlepony.unicopia.core.util.MagicalDamageSource;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;

public class UEffects {

    public static final StatusEffect FOOD_POISONING = new CustomStatusEffect(new Identifier(UnicopiaCore.MODID, "food_poisoning"), StatusEffectType.BENEFICIAL, 3484199)
            .setSilent()
            .direct((p, e, i) -> {
                StatusEffectInstance nausea = e.getStatusEffect(StatusEffects.NAUSEA);
                if (nausea == null) {
                    StatusEffectInstance foodEffect = e.getStatusEffect(p);
                    nausea = new StatusEffectInstance(StatusEffects.NAUSEA, foodEffect.getDuration(), foodEffect.getAmplifier(), foodEffect.isAmbient(), foodEffect.shouldShowParticles());

                    e.addPotionEffect(nausea);
                }

                e.damage(MagicalDamageSource.FOOD_POISONING, i);
            });
}
