package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.registry.Registries;

public interface UScreenHandlers {
    ScreenHandlerType<SpellbookScreenHandler> SPELL_BOOK = register("spell_book", new ExtendedScreenHandlerType<>(SpellbookScreenHandler::new));
    ScreenHandlerType<ShapingBenchScreenHandler> SHAPING_BENCH = register("shaping_bench", new ScreenHandlerType<>(ShapingBenchScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    static <T extends ScreenHandler> ScreenHandlerType<T> register(String name, ScreenHandlerType<T> type) {
        return Registry.register(Registries.SCREEN_HANDLER, Unicopia.id(name), type);
    }

    static void bootstrap() { }
}
