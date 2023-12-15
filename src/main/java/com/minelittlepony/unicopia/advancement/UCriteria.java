package com.minelittlepony.unicopia.advancement;

import net.minecraft.advancement.criterion.Criteria;

public interface UCriteria {
    CustomEventCriterion CUSTOM_EVENT = Criteria.register("unicopia:custom", new CustomEventCriterion());
    RaceChangeCriterion PLAYER_CHANGE_RACE = Criteria.register("unicopia:player_change_race", new RaceChangeCriterion());
    SendViaDragonBreathScrollCriterion SEND_DRAGON_BREATH = Criteria.register("unicopia:send_dragon_breath", new SendViaDragonBreathScrollCriterion());

    CustomEventCriterion.Trigger LOOK_INTO_SUN = CUSTOM_EVENT.createTrigger("look_into_sun");
    CustomEventCriterion.Trigger WEAR_SHADES = CUSTOM_EVENT.createTrigger("wear_shades");
    CustomEventCriterion.Trigger LIGHTNING_STRUCK = CUSTOM_EVENT.createTrigger("lightning_struck_player");
    CustomEventCriterion.Trigger EAT_TRICK_APPLE = CUSTOM_EVENT.createTrigger("eat_trick_apple");
    CustomEventCriterion.Trigger FEED_TRICK_APPLE = CUSTOM_EVENT.createTrigger("feed_trick_apple");
    CustomEventCriterion.Trigger SCREECH_SELF = CUSTOM_EVENT.createTrigger("screech_self");
    CustomEventCriterion.Trigger SCREECH_TWENTY_MOBS = CUSTOM_EVENT.createTrigger("screech_twenty_mobs");
    CustomEventCriterion.Trigger SPOOK_MOB = CUSTOM_EVENT.createTrigger("spook_mob");
    CustomEventCriterion.Trigger SHED_FEATHER = CUSTOM_EVENT.createTrigger("shed_feather");
    CustomEventCriterion.Trigger THROW_MUFFIN = CUSTOM_EVENT.createTrigger("throw_muffin");
    CustomEventCriterion.Trigger BREAK_WINDOW = CUSTOM_EVENT.createTrigger("break_window");
    CustomEventCriterion.Trigger KILL_PHANTOM_WHILE_FLYING = CUSTOM_EVENT.createTrigger("kill_phantom_while_flying");
    CustomEventCriterion.Trigger USE_CONSUMPTION = CUSTOM_EVENT.createTrigger("use_consumption");
    CustomEventCriterion.Trigger USE_SOULMATE = CUSTOM_EVENT.createTrigger("use_soulmate");
    CustomEventCriterion.Trigger SECOND_WIND = CUSTOM_EVENT.createTrigger("second_wind");
    CustomEventCriterion.Trigger DEFEAT_SOMBRA = CUSTOM_EVENT.createTrigger("defeat_sombra");
    CustomEventCriterion.Trigger LIGHT_ALTAR = CUSTOM_EVENT.createTrigger("light_altar");
    CustomEventCriterion.Trigger POWER_UP_HEART = CUSTOM_EVENT.createTrigger("power_up_heart");
    CustomEventCriterion.Trigger SPLIT_SEA = CUSTOM_EVENT.createTrigger("split_sea");
    CustomEventCriterion.Trigger RIDE_BALLOON = CUSTOM_EVENT.createTrigger("ride_balloon");
    CustomEventCriterion.Trigger TELEPORT_ABOVE_WORLD = CUSTOM_EVENT.createTrigger("teleport_above_world");

    static void bootstrap() { }
}
