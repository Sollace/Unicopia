package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.player.IOwned;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICaster<E extends EntityLivingBase> extends IOwned<E>, ILevelled {
    void setEffect(IMagicEffect effect);

    IMagicEffect getEffect();

    default boolean hasEffect() {
        return getEffect() != null;
    }

    /**
     * Gets the entity directly responsible for casting.
     */
    default Entity getEntity() {
        return getOwner();
    }

    default World getWorld() {
        return getEntity().getEntityWorld();
    }

    default BlockPos getOrigin() {
        return getEntity().getPosition();
    }
}
