package com.minelittlepony.unicopia.entity;

import net.minecraft.entity.Entity.RemovalReason;

public interface Removeable {
    void setRemovalReason(RemovalReason reason);
}
