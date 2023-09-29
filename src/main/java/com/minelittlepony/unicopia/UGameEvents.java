package com.minelittlepony.unicopia;

import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.world.event.GameEvent;

public interface UGameEvents {

    GameEvent PIE_STOMP = register("pie_stomp", 5);

    static GameEvent register(String name, int range) {
        Identifier id = Unicopia.id(name);
        return Registry.register(Registries.GAME_EVENT, id, new GameEvent(range));
    }

    static void bootstrap() {
    }
}
