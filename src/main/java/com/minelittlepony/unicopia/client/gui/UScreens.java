package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.container.UContainers;

import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;

public interface UScreens {

    static void bootstrap() {
        ScreenProviderRegistry.INSTANCE.registerFactory(UContainers.BAG_OF_HOLDING, BagOfHoldingScreen::new);
        ScreenProviderRegistry.INSTANCE.registerFactory(UContainers.SPELL_BOOK, SpellBookScreen::new);
    }
}
