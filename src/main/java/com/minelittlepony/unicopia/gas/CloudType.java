package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.CloudEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

public enum CloudType {
    NORMAL,
    PACKED,
    ENCHANTED;

    public boolean canInteract(Entity e) {
        if (e == null) {
            return false;
        }

        if (this == ENCHANTED) {
            return true;
        }

        if (e instanceof PlayerEntity) {

            if (this == PACKED) {
                return true;
            }

            return EquinePredicates.INTERACT_WITH_CLOUDS.test((PlayerEntity)e)
                || (EquinePredicates.MAGI.test(e) && CloudEntity.getFeatherEnchantStrength((PlayerEntity)e) > 0);
        }

        if (e instanceof ItemEntity) {
            return EquinePredicates.ITEM_INTERACT_WITH_CLOUDS.test((ItemEntity)e);
        }

        if (e instanceof CloudEntity && e.hasVehicle()) {
            return canInteract(e.getVehicle());
        }

        return false;
    }
}