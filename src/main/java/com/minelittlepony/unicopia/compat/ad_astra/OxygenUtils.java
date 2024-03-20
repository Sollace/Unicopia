package com.minelittlepony.unicopia.compat.ad_astra;

import net.minecraft.entity.Entity;

public final class OxygenUtils {
    public static OxygenApi API = entity -> false;

    public interface OxygenApi {
        boolean hasOxygen(Entity entity);
    }
}
