package com.minelittlepony.unicopia;

import java.util.function.Predicate;

import com.minelittlepony.unicopia.entity.CloudEntity;
import com.minelittlepony.unicopia.entity.Ponylike;
import com.minelittlepony.unicopia.gas.Gas;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;

public interface EquinePredicates {
    Predicate<Entity> IS_CLOUD = e -> e instanceof CloudEntity;
    Predicate<Entity> IS_PLAYER = e -> e instanceof PlayerEntity;
    Predicate<Entity> IS_VALID_ITEM = entity -> entity instanceof ItemEntity && entity.isAlive() && entity.age > 1;

    Predicate<Entity> RACE_INTERACT_WITH_CLOUDS = entity -> Ponylike.of(entity).getSpecies().canInteractWithClouds();

    Predicate<Entity> PLAYER_UNICORN = IS_PLAYER.and(entity -> Ponylike.of(entity).getSpecies().canCast());
    Predicate<Entity> PLAYER_CHANGELING = IS_PLAYER.and(entity -> Ponylike.of(entity).getSpecies() == Race.CHANGELING);
    Predicate<Entity> PLAYER_PEGASUS = IS_PLAYER.and(entity -> ((PlayerEntity)entity).abilities.creativeMode || RACE_INTERACT_WITH_CLOUDS.test(entity));

    Predicate<Entity> ITEM_INTERACT_WITH_CLOUDS = IS_VALID_ITEM.and(RACE_INTERACT_WITH_CLOUDS.or(e -> {
        return CloudEntity.getFeatherEnchantStrength(((ItemEntity)e).getStack()) > 0
                || (((ItemEntity)e).getStack().getItem() instanceof BlockItem
                && ((BlockItem)((ItemEntity)e).getStack().getItem()).getBlock() instanceof Gas);
    }));

    Predicate<Entity> ENTITY_INTERACT_WITH_CLOUDS = PLAYER_PEGASUS.or(ITEM_INTERACT_WITH_CLOUDS);
    Predicate<Entity> ENTITY_WALK_ON_CLOUDS = entity -> entity instanceof LivingEntity && CloudEntity.getFeatherEnchantStrength((LivingEntity)entity) > 0;

    Predicate<Entity> ENTITY_INTERACT_WITH_CLOUD_BLOCKS = IS_CLOUD.or(PLAYER_PEGASUS).or(ENTITY_WALK_ON_CLOUDS).or(ITEM_INTERACT_WITH_CLOUDS).or(entity -> {
        return entity != null && EquinePredicates.ENTITY_INTERACT_WITH_CLOUD_BLOCKS.test(entity.getVehicle());
    });
}
