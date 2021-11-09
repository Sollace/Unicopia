package com.minelittlepony.unicopia.container;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.SimpleClientHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public interface UScreenHandlers {
    ScreenHandlerType<SpellBookScreenHandler> SPELL_BOOK = register("spell_book", SpellBookScreenHandler::new);

    static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, SimpleClientHandlerFactory<T> factory) {
        return ScreenHandlerRegistry.registerSimple(new Identifier("unicopia", name), factory);
    }

    static void bootstrap() { }
}
