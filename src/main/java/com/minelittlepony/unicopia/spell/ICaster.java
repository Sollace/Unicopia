package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.player.IOwned;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public interface ICaster<E extends EntityLivingBase> extends IOwned<E> {
    void setEffect(IMagicEffect effect);

    IMagicEffect getEffect();

    default boolean hasEffect() {
        return getEffect() != null;
    }

    default Entity getEntity() {
        return getOwner();
    }
}
