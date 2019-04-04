package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.entity.EntityCloud;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

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

        if (e instanceof EntityPlayer) {

            if (this == PACKED) {
                return true;
            }

            return Predicates.INTERACT_WITH_CLOUDS.test((EntityPlayer)e)
                || (Predicates.MAGI.test(e) && EntityCloud.getFeatherEnchantStrength((EntityPlayer)e) > 0);
        }

        if (e instanceof EntityItem) {
            return Predicates.ITEM_INTERACT_WITH_CLOUDS.test((EntityItem)e);
        }

        if (e instanceof EntityCloud && e.isRiding()) {
            return canInteract(e.getRidingEntity());
        }

        return false;
    }
}