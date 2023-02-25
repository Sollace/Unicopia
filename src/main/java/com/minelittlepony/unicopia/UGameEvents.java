package com.minelittlepony.unicopia;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.GameEvent;

public interface UGameEvents {

    GameEvent PIE_STOMP = register("pie_stomp", 5);

    static GameEvent register(String name, int range) {
        Identifier id = Unicopia.id(name);
        return Registry.register(Registry.GAME_EVENT, id, new GameEvent(id.toString(), range));
    }

    static void bootstrap() {
    }
}
