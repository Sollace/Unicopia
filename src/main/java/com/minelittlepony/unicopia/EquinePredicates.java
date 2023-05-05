package com.minelittlepony.unicopia;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;

public interface EquinePredicates {
    Predicate<Entity> IS_PLAYER = e -> e instanceof PlayerEntity;

    Predicate<Entity> RACE_INTERACT_WITH_CLOUDS = raceMatches(Race::canInteractWithClouds);

    Predicate<Entity> PLAYER_EARTH = IS_PLAYER.and(ofRace(Race.EARTH));
    Predicate<Entity> PLAYER_BAT = IS_PLAYER.and(ofRace(Race.BAT)).or(physicalRaceMatches(Race.BAT::equals));
    Predicate<Entity> PLAYER_UNICORN = IS_PLAYER.and(raceMatches(Race::canCast));
    Predicate<Entity> PLAYER_CHANGELING = IS_PLAYER.and(ofRace(Race.CHANGELING));
    Predicate<Entity> PLAYER_PEGASUS = IS_PLAYER.and(e -> ((PlayerEntity)e).getAbilities().creativeMode || RACE_INTERACT_WITH_CLOUDS.test(e));

    Predicate<Entity> PLAYER_CAN_USE_EARTH = IS_PLAYER.and(raceMatches(Race::canUseEarth));
    Predicate<Entity> IS_CASTER = e -> !e.isRemoved() && (e instanceof Caster || IS_PLAYER.test(e));
    Predicate<Entity> IS_PLACED_SPELL = e -> e instanceof Caster && !e.isRemoved();

    Predicate<LivingEntity> HAS_WANT_IT_NEED_IT = e -> {
        return EnchantmentHelper.getEquipmentLevel(UEnchantments.WANT_IT_NEED_IT, e) > 0
            || EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, e.getOffHandStack()) > 0
            || EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, e.getMainHandStack()) > 0;
    };

    Predicate<Entity> VALID_FOR_DISGUISE = EntityPredicates.EXCEPT_SPECTATOR.and(e -> !(e instanceof LightningEntity || e instanceof AbstractDecorationEntity));

    static Predicate<Entity> ofRace(Race race) {
        return raceMatches(race::equals);
    }

    static Predicate<Entity> raceMatches(Predicate<Race> predicate) {
        return e -> Equine.of(e).map(Equine::getSpecies).filter(predicate).isPresent();
    }

    static Predicate<Entity> physicalRaceMatches(Predicate<Race> predicate) {
        return e -> Pony.of(e).map(Pony::getActualSpecies).filter(predicate).isPresent();
    }
}
