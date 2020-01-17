package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.util.MagicalDamageSource;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;

public class UEffects {

    public static final StatusEffect FOOD_POISONING = new UPotion(Unicopia.MODID, "food_poisoning", true, 3484199)
            .setIconIndex(3, 1)
            .setSilent()
            .setEffectiveness(0.25)
            .setApplicator((p, e, i) -> {

                StatusEffectInstance nausea = e.getStatusEffect(StatusEffects.NAUSEA);
                if (nausea == null) {
                    StatusEffectInstance foodEffect = e.getStatusEffect(p);
                    nausea = new StatusEffectInstance(StatusEffects.NAUSEA, foodEffect.getDuration(), foodEffect.getAmplifier(), foodEffect.getIsAmbient(), foodEffect.doesShowParticles());

                    e.addPotionEffect(nausea);
                }

                e.damage(MagicalDamageSource.FOOD_POISONING, i);
            });
}
