package com.minelittlepony.unicopia;

import java.util.function.Predicate;

import javax.annotation.Nullable;

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

    Predicate<Entity> RACE_INTERACT_WITH_CLOUDS = entity -> Equine.of(entity).getSpecies().canInteractWithClouds();

    Predicate<Entity> PLAYER_EARTH = IS_PLAYER.and(entity -> Equine.of(entity).getSpecies() == Race.EARTH);
    Predicate<Entity> PLAYER_BAT = IS_PLAYER.and(entity -> Equine.of(entity).getSpecies() == Race.BAT);
    Predicate<Entity> PLAYER_UNICORN = IS_PLAYER.and(entity -> Equine.of(entity).getSpecies().canCast());
    Predicate<Entity> PLAYER_CHANGELING = IS_PLAYER.and(entity -> Equine.of(entity).getSpecies() == Race.CHANGELING);
    Predicate<Entity> PLAYER_PEGASUS = IS_PLAYER.and(entity -> ((PlayerEntity)entity).abilities.creativeMode || RACE_INTERACT_WITH_CLOUDS.test(entity));

    Predicate<Entity> IS_CASTER = e -> !e.removed && (e instanceof Caster || PLAYER_UNICORN.test(e));

    Predicate<LivingEntity> HAS_WANT_IT_NEED_IT = e -> EnchantmentHelper.getEquipmentLevel(UEnchantments.WANT_IT_NEED_IT, e) > 0;

    static Predicate<Entity> carryingSpell(@Nullable SpellType<?> type) {
        return IS_PLAYER.and(entity -> Pony.of((PlayerEntity)entity).getSpellSlot().get(type, false).isPresent());
    }
}
