package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.entity.EntityCloud;

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

            return Predicates.INTERACT_WITH_CLOUDS.test((PlayerEntity)e)
                || (Predicates.MAGI.test(e) && EntityCloud.getFeatherEnchantStrength((PlayerEntity)e) > 0);
        }

        if (e instanceof ItemEntity) {
            return Predicates.ITEM_INTERACT_WITH_CLOUDS.test((ItemEntity)e);
        }

        if (e instanceof EntityCloud && e.hasVehicle()) {
            return canInteract(e.getVehicle());
        }

        return false;
    }
}