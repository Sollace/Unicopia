package com.minelittlepony.unicopia;

import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.Registries;
import net.minecraft.world.event.GameEvent;

public interface UGameEvents {

    RegistryEntry<GameEvent> PIE_STOMP = register("pie_stomp", 5);

    static RegistryEntry<GameEvent> register(String name, int range) {
        Identifier id = Unicopia.id(name);
        return Registry.registerReference(Registries.GAME_EVENT, id, new GameEvent(range));
    }

    static void bootstrap() {
    }
}
