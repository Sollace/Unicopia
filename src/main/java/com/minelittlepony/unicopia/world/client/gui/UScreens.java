package com.minelittlepony.unicopia.world.client.gui;

import com.minelittlepony.unicopia.world.container.UContainers;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry.Factory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public interface UScreens {
    static void bootstrap() {
        register(UContainers.BAG_OF_HOLDING, BagOfHoldingScreen::new);
        register(UContainers.SPELL_BOOK, SpellBookScreen::new);
    }

    // TODO: farbreic :tiny_potato:
    static <
        H extends ScreenHandler,
        S extends Screen & ScreenHandlerProvider<H>>
        void register(
                ScreenHandlerType<? extends H> type,
                Factory<? super H, S> screenFactory) {
        ScreenRegistry.register(type, screenFactory);
    }
}
