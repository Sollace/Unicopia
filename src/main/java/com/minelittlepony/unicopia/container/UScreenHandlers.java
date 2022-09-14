package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;

public interface UScreenHandlers {
    ScreenHandlerType<SpellbookScreenHandler> SPELL_BOOK = register("spell_book", new ExtendedScreenHandlerType<>(SpellbookScreenHandler::new));

    static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, ScreenHandlerType<T> type) {
        return Registry.register(Registry.SCREEN_HANDLER, Unicopia.id(name), type);
    }

    static void bootstrap() { }
}
