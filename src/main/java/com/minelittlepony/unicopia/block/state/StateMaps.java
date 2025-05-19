package com.minelittlepony.unicopia.block.state;

import com.minelittlepony.unicopia.Unicopia;

public interface StateMaps {
    BlockStateConverter SNOW_PILED = of("snow_piled");
    BlockStateConverter ICE_AFFECTED = of("ice");
    BlockStateConverter SILVERFISH_AFFECTED = of("infestation");
    BlockStateConverter FIRE_AFFECTED = of("fire");
    BlockStateConverter BURNABLE = of("burnable");
    ReversableBlockStateConverter HELLFIRE_AFFECTED = of("hellfire");

    private static ReversableBlockStateConverter of(String name) {
        return BlockStateConverter.of(Unicopia.id(name));
    }
}
