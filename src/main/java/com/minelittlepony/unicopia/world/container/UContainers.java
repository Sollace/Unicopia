package com.minelittlepony.unicopia.world.container;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry.SimpleClientHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public interface UContainers {

    ScreenHandlerType<BagOfHoldingContainer> BAG_OF_HOLDING = register("bag_of_holding", BagOfHoldingContainer::new);
    ScreenHandlerType<SpellBookContainer> SPELL_BOOK = register("spell_book", SpellBookContainer::new);

    static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, SimpleClientHandlerFactory<T> factory) {
        return ScreenHandlerRegistry.registerSimple(new Identifier("unicopia", name), factory);
    }

    static void bootstrap() { }
}
