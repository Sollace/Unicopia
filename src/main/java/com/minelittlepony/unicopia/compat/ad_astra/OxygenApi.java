package com.minelittlepony.unicopia.compat.ad_astra;

import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.entity.Entity;

public interface OxygenApi {
    AtomicReference<OxygenApi> API = new AtomicReference<>(entity -> false);

    boolean hasOxygen(Entity entity);
}
