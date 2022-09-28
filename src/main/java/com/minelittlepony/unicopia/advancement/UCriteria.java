package com.minelittlepony.unicopia.advancement;

import com.minelittlepony.unicopia.mixin.MixinCriteria;

import net.minecraft.advancement.criterion.Criterion;

public interface UCriteria {
    CustomEventCriterion CUSTOM_EVENT = register(new CustomEventCriterion());
    RaceChangeCriterion PLAYER_CHANGE_RACE = register(new RaceChangeCriterion());

    CustomEventCriterion.Trigger LOOK_INTO_SUN = CUSTOM_EVENT.createTrigger("look_into_sun");
    CustomEventCriterion.Trigger WEAR_SHADES = CUSTOM_EVENT.createTrigger("wear_shades");
    CustomEventCriterion.Trigger LIGHTNING_STRUCK = CUSTOM_EVENT.createTrigger("lightning_struck_player");
    CustomEventCriterion.Trigger EAT_TRICK_APPLE = CUSTOM_EVENT.createTrigger("eat_trick_apple");
    CustomEventCriterion.Trigger FEED_TRICK_APPLE = CUSTOM_EVENT.createTrigger("feed_trick_apple");
    CustomEventCriterion.Trigger SCREECH_SELF = CUSTOM_EVENT.createTrigger("screech_self");
    CustomEventCriterion.Trigger SCREECH_TWENTY_MOBS = CUSTOM_EVENT.createTrigger("screech_twenty_mobs");
    CustomEventCriterion.Trigger SHED_FEATHER = CUSTOM_EVENT.createTrigger("shed_feather");
    CustomEventCriterion.Trigger THROW_MUFFIN = CUSTOM_EVENT.createTrigger("throw_muffin");
    CustomEventCriterion.Trigger SEND_OATS = CUSTOM_EVENT.createTrigger("send_oats");
    CustomEventCriterion.Trigger RECEIVE_OATS = CUSTOM_EVENT.createTrigger("receive_oats");

    private static <T extends Criterion<?>> T register(T obj) {
        return MixinCriteria.register(obj);
    }

    static void bootstrap() { }
}
