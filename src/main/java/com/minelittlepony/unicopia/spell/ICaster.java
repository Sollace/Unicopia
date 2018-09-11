package com.minelittlepony.unicopia.spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public interface ICaster<E extends EntityLivingBase> {
    void setEffect(IMagicEffect effect);

    IMagicEffect getEffect();

    default boolean hasEffect() {
        return getEffect() != null;
    }

    default void setOwner(EntityLivingBase owner) {

    }

    E getOwner();

    default Entity getEntity() {
        return getOwner();
    }
}
