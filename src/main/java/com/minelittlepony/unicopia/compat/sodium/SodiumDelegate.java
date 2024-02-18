package com.minelittlepony.unicopia.compat.sodium;

import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;

public interface SodiumDelegate {
    SodiumDelegate EMPTY = new SodiumDelegate() {};

    static SodiumDelegate getInstance() {
        if (FabricLoader.getInstance().isModLoaded("sodium")) {
            return Impl.INSTANCE;
        }
        return EMPTY;
    }

    default TriState isFancyLeavesOrBetter() {
        return TriState.DEFAULT;
    }
}
