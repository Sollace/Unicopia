package com.minelittlepony.unicopia.entity.duck;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;

public interface EntityDuck extends LavaAffine {
    void setRemovalReason(RemovalReason reason);

    void setVehicle(Entity vehicle);

    @Override
    default void setLavaAffine(boolean lavaAffine) {

    }
}
