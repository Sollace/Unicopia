package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.potion.Potion;

public class UEffects {

    public static final DamageSource food_poisoning = new DamageSource("food_poisoning").bypassesArmor();

    public static final StatusEffect FOOD_POISONING = new UPotion(Unicopia.MODID, "food_poisoning", true, 3484199)
            .setIconIndex(3, 1)
            .setSilent()
            .setEffectiveness(0.25)
            .setApplicator((p, e, i) -> {

                StatusEffectInstance nausea = e.getActivePotionEffect(StatusEffects.NAUSEA);
                if (nausea == null) {
                    StatusEffect foodEffect = e.getActivePotionEffect(p);
                    nausea = new StatusEffectInstance(StatusEffects.NAUSEA, foodEffect.getDuration(), foodEffect.getAmplifier(), foodEffect.getIsAmbient(), foodEffect.doesShowParticles());

                    e.addPotionEffect(nausea);
                }

                e.attackEntityFrom(food_poisoning, i);
            });
}
