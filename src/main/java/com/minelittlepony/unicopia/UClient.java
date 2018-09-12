package com.minelittlepony.unicopia;

import net.minecraftforge.fml.common.FMLCommonHandler;

public interface UClient {
    static boolean isClientSide() {
        return FMLCommonHandler.instance().getSide().isClient();
    }
}
