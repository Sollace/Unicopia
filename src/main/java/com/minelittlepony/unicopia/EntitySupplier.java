package com.minelittlepony.unicopia;

import net.minecraft.entity.Entity;

public interface EntitySupplier {
    /**
     * Gets the owner that holds this object.
     */
    Entity getEntity();
}
