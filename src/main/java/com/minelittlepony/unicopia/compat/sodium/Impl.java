package com.minelittlepony.unicopia.compat.sodium;

import me.jellysquid.mods.sodium.client.SodiumClientMod;
import net.fabricmc.fabric.api.util.TriState;

public class Impl implements SodiumDelegate {
    static Impl INSTANCE = new Impl();

    @Override
    public TriState isFancyLeavesOrBetter() {
        return switch (SodiumClientMod.options().quality.leavesQuality) {
            case FAST -> TriState.FALSE;
            case FANCY -> TriState.TRUE;
            case DEFAULT -> TriState.DEFAULT;
        };
    }
}
