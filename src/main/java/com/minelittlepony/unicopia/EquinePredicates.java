package com.minelittlepony.unicopia;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class EquinePredicates {
    public static final Predicate<PlayerEntity> INTERACT_WITH_CLOUDS = player -> {
        return player != null && SpeciesList.instance().getPlayer(player).getSpecies().canInteractWithClouds();
    };

    public static final Predicate<Entity> MAGI = entity -> {
        return entity instanceof PlayerEntity && SpeciesList.instance().getPlayer((PlayerEntity)entity).getSpecies().canCast();
    };

    public static final Predicate<Entity> ITEMS = entity -> {
        return entity instanceof ItemEntity && entity.isAlive() && entity.age > 1;
    };

    public static final Predicate<ItemEntity> ITEM_INTERACT_WITH_CLOUDS = item -> {
        return ITEMS.test(item) && SpeciesList.instance().getEntity(item).getSpecies().canInteractWithClouds();
    };

    public static final Predicate<Entity> ENTITY_INTERACT_WITH_CLOUDS = entity -> {
        return entity != null && (
                    (entity instanceof PlayerEntity && INTERACT_WITH_CLOUDS.test((PlayerEntity)entity))
                 || (entity instanceof ItemEntity && ITEM_INTERACT_WITH_CLOUDS.test((ItemEntity)entity))
              );
    };

    public static final Predicate<Entity> BUGGY = entity -> {
        return entity instanceof PlayerEntity
                && SpeciesList.instance().getPlayer((PlayerEntity)entity).getSpecies() == Race.CHANGELING;
    };

    public static PlayerEntity getPlayerFromEntity(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return (PlayerEntity) entity;
        }

        if (entity instanceof ItemEntity) {
            ItemEntity item = (ItemEntity)entity;
            if (item.getOwner() != null) {
                return item.getEntityWorld().getPlayerByUuid(item.getOwner());
            }
        }

        return null;
    }
}
