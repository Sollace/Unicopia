package com.minelittlepony.unicopia.advancement;

import com.minelittlepony.unicopia.mixin.MixinCriteria;

import net.minecraft.advancement.criterion.Criterion;

public interface UCriteria {
    CustomEventCriterion CUSTOM_EVENT = register(new CustomEventCriterion());

    CustomEventCriterion.Trigger LOOK_INTO_SUN = CUSTOM_EVENT.createTrigger("look_into_sun");
    CustomEventCriterion.Trigger WEAR_SHADES = CUSTOM_EVENT.createTrigger("wear_shades");
    CustomEventCriterion.Trigger LIGHTNING_STRUCK = CUSTOM_EVENT.createTrigger("lightning_struck_player");
    CustomEventCriterion.Trigger EAT_TRICK_APPLE = CUSTOM_EVENT.createTrigger("eat_trick_apple");
    CustomEventCriterion.Trigger FEED_TRICK_APPLE = CUSTOM_EVENT.createTrigger("feed_trick_apple");

    private static <T extends Criterion<?>> T register(T obj) {
        return MixinCriteria.register(obj);
    }

    static void bootstrap() { }
}
