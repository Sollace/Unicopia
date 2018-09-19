package com.minelittlepony.unicopia;

import com.google.common.base.Predicate;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

public final class Predicates {
    public static final Predicate<EntityPlayer> INTERACT_WITH_CLOUDS = player -> {
        return player != null && PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies().canInteractWithClouds();
    };

    public static final Predicate<EntityPlayer> MAGI = player -> {
        return player != null && PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies().canCast();
    };

    public static final Predicate<Entity> ITEMS = entity -> {
        return entity.isEntityAlive() && entity instanceof EntityItem;
    };

    public static final Predicate<EntityItem> ITEM_INTERACT_WITH_CLOUDS = item -> {
        return ITEMS.test(item) && PlayerSpeciesList.instance().getEntity(item).getPlayerSpecies().canInteractWithClouds();
    };

    public static final Predicate<Entity> ENTITY_INTERACT_WITH_CLOUDS = entity -> {
        return entity != null && (
                    (entity instanceof EntityPlayer && INTERACT_WITH_CLOUDS.test((EntityPlayer)entity))
                 || (entity instanceof EntityItem && ITEM_INTERACT_WITH_CLOUDS.test((EntityItem)entity))
              );
    };

    public static EntityPlayer getPlayerFromEntity(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return (EntityPlayer) entity;
        }

        if (entity instanceof EntityItem) {
            EntityItem item = (EntityItem)entity;
            if (item.getOwner() != null) {
                return item.getEntityWorld().getPlayerEntityByName(item.getOwner());
            }
        }

        return null;
    }
}
