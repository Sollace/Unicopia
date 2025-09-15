package com.minelittlepony.unicopia.client.minelittlepony;

import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityLookup;

public interface EntityLookupAccessor {
    EntityLookup<Entity> callGetEntityLookup();
}
