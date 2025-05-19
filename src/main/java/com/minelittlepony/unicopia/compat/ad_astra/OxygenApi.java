package com.minelittlepony.unicopia.compat.ad_astra;

import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface OxygenApi {
    AtomicReference<OxygenApi> API = new AtomicReference<>((world, pos) -> false);

    boolean hasOxygen(World world, BlockPos pos);
}
