package com.minelittlepony.unicopia;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.entity.Equine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public interface EquinePredicates {
    Predicate<Entity> IS_PLAYER = e -> e instanceof PlayerEntity;

    Predicate<Entity> RACE_INTERACT_WITH_CLOUDS = entity -> Equine.of(entity).getSpecies().canInteractWithClouds();

    Predicate<Entity> PLAYER_UNICORN = IS_PLAYER.and(entity -> Equine.of(entity).getSpecies().canCast());
    Predicate<Entity> PLAYER_CHANGELING = IS_PLAYER.and(entity -> Equine.of(entity).getSpecies() == Race.CHANGELING);
    Predicate<Entity> PLAYER_PEGASUS = IS_PLAYER.and(entity -> ((PlayerEntity)entity).abilities.creativeMode || RACE_INTERACT_WITH_CLOUDS.test(entity));

}
