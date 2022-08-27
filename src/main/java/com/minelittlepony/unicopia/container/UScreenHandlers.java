package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;

public interface UScreenHandlers {
    ScreenHandlerType<SpellbookScreenHandler> SPELL_BOOK = register("spell_book", SpellbookScreenHandler::new);

    static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registry.SCREEN_HANDLER, Unicopia.id(name), new ScreenHandlerType<>(factory));
    }

    static void bootstrap() { }
}
