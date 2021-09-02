package com.minelittlepony.unicopia;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface EquinePredicates {
    Predicate<Entity> IS_PLAYER = e -> e instanceof PlayerEntity;

    Predicate<Entity> RACE_INTERACT_WITH_CLOUDS = raceMatches(Race::canInteractWithClouds);

    Predicate<Entity> PLAYER_EARTH = IS_PLAYER.and(ofRace(Race.EARTH));
    Predicate<Entity> PLAYER_BAT = IS_PLAYER.and(ofRace(Race.BAT));
    Predicate<Entity> PLAYER_UNICORN = IS_PLAYER.and(raceMatches(Race::canCast));
    Predicate<Entity> PLAYER_CHANGELING = IS_PLAYER.and(ofRace(Race.CHANGELING));
    Predicate<Entity> PLAYER_PEGASUS = IS_PLAYER.and(e -> ((PlayerEntity)e).getAbilities().creativeMode || RACE_INTERACT_WITH_CLOUDS.test(e));

    Predicate<Entity> PLAYER_CAN_USE_EARTH = IS_PLAYER.and(raceMatches(Race::canUseEarth));
    Predicate<Entity> IS_CASTER = e -> !e.isRemoved() && (e instanceof Caster || PLAYER_UNICORN.test(e));

    Predicate<LivingEntity> HAS_WANT_IT_NEED_IT = e -> EnchantmentHelper.getEquipmentLevel(UEnchantments.WANT_IT_NEED_IT, e) > 0;

    static Predicate<Entity> carryingSpell(@Nullable SpellType<?> type) {
        return IS_PLAYER.and(e -> Pony.of((PlayerEntity)e).getSpellSlot().get(type, false).isPresent());
    }

    static Predicate<Entity> ofRace(Race race) {
        return raceMatches(race::equals);
    }

    static Predicate<Entity> raceMatches(Predicate<Race> predicate) {
        return e -> Equine.of(e).map(Equine::getSpecies).filter(predicate).isPresent();
    }
}
