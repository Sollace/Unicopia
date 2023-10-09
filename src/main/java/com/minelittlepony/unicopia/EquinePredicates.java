package com.minelittlepony.unicopia;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.MagicImmune;
import com.minelittlepony.unicopia.item.enchantment.WantItNeedItEnchantment;

import net.minecraft.entity.*;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;

public interface EquinePredicates {
    Predicate<Entity> IS_PLAYER = e -> e instanceof PlayerEntity;
    Predicate<Entity> BAT = physicalRaceMatches(Race.BAT::equals);
    Predicate<Entity> CHANGELING = physicalRaceMatches(Race.CHANGELING::equals);

    Predicate<Entity> RACE_INTERACT_WITH_CLOUDS = raceMatches(Race::canInteractWithClouds);
    Predicate<Entity> RAGING = IS_PLAYER.and(SpellType.RAGE::isOn);

    Predicate<Entity> PLAYER_EARTH = IS_PLAYER.and(ofRace(Race.EARTH));
    Predicate<Entity> PLAYER_BAT = IS_PLAYER.and(BAT);
    Predicate<Entity> PLAYER_UNICORN = IS_PLAYER.and(raceMatches(Race::canCast));
    Predicate<Entity> PLAYER_CHANGELING = IS_PLAYER.and(ofRace(Race.CHANGELING));
    Predicate<Entity> PLAYER_KIRIN = IS_PLAYER.and(ofRace(Race.KIRIN));
    Predicate<Entity> PLAYER_PEGASUS = IS_PLAYER.and(e -> ((PlayerEntity)e).getAbilities().creativeMode || RACE_INTERACT_WITH_CLOUDS.test(e));

    Predicate<Entity> PLAYER_CAN_USE_EARTH = IS_PLAYER.and(raceMatches(Race::canUseEarth));
    Predicate<Entity> IS_CASTER = e -> !e.isRemoved() && (e instanceof Caster || IS_PLAYER.test(e));
    Predicate<Entity> IS_PLACED_SPELL = e -> e instanceof Caster && !e.isRemoved();

    Predicate<Entity> IS_MAGIC_IMMUNE = e -> (e instanceof MagicImmune || !(e instanceof LivingEntity)) && !(e instanceof ItemEntity);
    Predicate<Entity> EXCEPT_MAGIC_IMMUNE = IS_MAGIC_IMMUNE.negate();
    Predicate<Entity> VALID_LIVING_AND_NOT_MAGIC_IMMUNE = EntityPredicates.VALID_LIVING_ENTITY.and(EXCEPT_MAGIC_IMMUNE);

    Predicate<LivingEntity> LIVING_HAS_WANT_IT_NEED_IT = e -> WantItNeedItEnchantment.getLevel(e) > 0;
    Predicate<Entity> VALID_FOR_DISGUISE = EntityPredicates.EXCEPT_SPECTATOR.and(e -> !(e instanceof LightningEntity || e instanceof AbstractDecorationEntity));

    static Predicate<Entity> ofRace(Race race) {
        return raceMatches(race::equals);
    }

    static Predicate<Entity> raceMatches(Predicate<Race> predicate) {
        return e -> Equine.of(e).filter(pony -> pony.getCompositeRace().any(predicate)).isPresent();
    }

    static Predicate<Entity> physicalRaceMatches(Predicate<Race> predicate) {
        return e -> Equine.of(e).filter(pony -> predicate.test(pony.getCompositeRace().physical())).isPresent();
    }
}
