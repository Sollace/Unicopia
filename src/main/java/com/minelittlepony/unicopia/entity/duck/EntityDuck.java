package com.minelittlepony.unicopia.entity.duck;

import com.minelittlepony.unicopia.compat.pehkui.PehkuiEntityExtensions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;

public interface EntityDuck extends LavaAffine, PehkuiEntityExtensions {
    void setRemovalReason(RemovalReason reason);

    void setVehicle(Entity vehicle);

    @Override
    default void setLavaAffine(boolean lavaAffine) {

    }
}
