package com.minelittlepony.unicopia.block.state;

import com.minelittlepony.unicopia.Unicopia;

public interface StateMaps {
    StateMapLoader.Indirect SNOW_PILED = of("snow_piled");
    StateMapLoader.Indirect ICE_AFFECTED = of("ice");
    StateMapLoader.Indirect SILVERFISH_AFFECTED = of("infestation");
    StateMapLoader.Indirect FIRE_AFFECTED = of("fire");
    StateMapLoader.Indirect BURNABLE = of("burnable");
    StateMapLoader.Indirect HELLFIRE_AFFECTED = of("hellfire");

    private static StateMapLoader.Indirect of(String name) {
        return BlockStateConverter.of(Unicopia.id(name));
    }
}
