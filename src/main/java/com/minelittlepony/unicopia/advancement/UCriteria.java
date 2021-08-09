package com.minelittlepony.unicopia.advancement;

import com.minelittlepony.unicopia.mixin.MixinCriteria;

import net.minecraft.advancement.criterion.Criterion;

public interface UCriteria {
    CustomEventCriterion CUSTOM_EVENT = register(new CustomEventCriterion());

    CustomEventCriterion.Trigger LOOK_INTO_SUN = CUSTOM_EVENT.createTrigger("look_into_sun");
    CustomEventCriterion.Trigger WEAR_SHADES = CUSTOM_EVENT.createTrigger("wear_shades");

    private static <T extends Criterion<?>> T register(T obj) {
        return MixinCriteria.register(obj);
    }

    static void bootstrap() { }
}
