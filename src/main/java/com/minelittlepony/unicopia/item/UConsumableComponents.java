package com.minelittlepony.unicopia.item;

import java.util.List;

import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.sound.SoundEvents;

public interface UConsumableComponents {
    ConsumableComponent OATMEAL = ConsumableComponents.food()
            .consumeEffect(new ApplyEffectsConsumeEffect(List.of(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 1200, 1)), 1))
            .consumeEffect(new ApplyEffectsConsumeEffect(List.of(new StatusEffectInstance(StatusEffects.STRENGTH, 1200, 1)), 0.3F))
            .consumeEffect(new ApplyEffectsConsumeEffect(List.of(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1)), 0.2F)).build();

    ConsumableComponent ZAP_BULB = ConsumableComponents.food()
            .consumeEffect(new ApplyEffectsConsumeEffect(List.of(
                    new StatusEffectInstance(StatusEffects.POISON, 100, 0),
                    new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0),
                    new StatusEffectInstance(StatusEffects.BAD_OMEN, 100, 0)
            ), 0.6F))
            .finishSound(SoundEvents.ITEM_OMINOUS_BOTTLE_DISPOSE)
            .build();

    ConsumableComponent SNACK = ConsumableComponents.food().consumeSeconds(0.8F).build();

    static ConsumableComponent poisonedFish(float poisonChance, float hungerChance, float nauseaChance) {
        return ConsumableComponents.food()
                .consumeEffect(new ApplyEffectsConsumeEffect(List.of(new StatusEffectInstance(StatusEffects.POISON, 1200, 1)), poisonChance))
                .consumeEffect(new ApplyEffectsConsumeEffect(List.of(new StatusEffectInstance(StatusEffects.HUNGER, 300, 2)), hungerChance))
                .consumeEffect(new ApplyEffectsConsumeEffect(List.of(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0)), nauseaChance))
                .build();
    }
}
