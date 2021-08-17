package com.minelittlepony.unicopia.item.toxin;

import com.minelittlepony.unicopia.util.CustomStatusEffect;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;

public interface UEffects {

    StatusEffect FOOD_POISONING = new CustomStatusEffect.Builder(new Identifier("unicopia", "food_poisoning"), StatusEffectType.HARMFUL, 3484199).direct((p, e, i) -> {
        StatusEffectInstance nausea = e.getStatusEffect(StatusEffects.NAUSEA);
        if (nausea == null) {
            StatusEffectInstance foodEffect = e.getStatusEffect(p);
            nausea = new StatusEffectInstance(StatusEffects.NAUSEA, foodEffect.getDuration(),
                    foodEffect.getAmplifier(),
                    true,
                    foodEffect.shouldShowParticles(),
                    false
            );

            e.addStatusEffect(nausea);
        }

        e.damage(MagicalDamageSource.FOOD_POISONING, i);
    }).build();
}
