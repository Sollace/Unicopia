package com.minelittlepony.unicopia.advancement;

import net.minecraft.advancement.criterion.Criteria;

public interface UCriteria {
    CustomEventCriterion CUSTOM_EVENT = Criteria.register(new CustomEventCriterion());
    RaceChangeCriterion PLAYER_CHANGE_RACE = Criteria.register(new RaceChangeCriterion());

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
    CustomEventCriterion.Trigger BREAK_WINDOW = CUSTOM_EVENT.createTrigger("break_window");
    CustomEventCriterion.Trigger KILL_PHANTOM_WHILE_FLYING = CUSTOM_EVENT.createTrigger("kill_phantom_while_flying");
    CustomEventCriterion.Trigger USE_CONSUMPTION = CUSTOM_EVENT.createTrigger("use_consumption");
    CustomEventCriterion.Trigger USE_SOULMATE = CUSTOM_EVENT.createTrigger("use_soulmate");
    CustomEventCriterion.Trigger SECOND_WIND = CUSTOM_EVENT.createTrigger("second_wind");

    static void bootstrap() { }
}
