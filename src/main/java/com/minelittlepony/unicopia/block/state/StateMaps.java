package com.minelittlepony.unicopia.block.state;

import com.minelittlepony.unicopia.Unicopia;

public interface StateMaps {
    BlockStateConverter.Named SNOW_PILED = of("snow_piled");
    BlockStateConverter.Named ICE_AFFECTED = of("ice");
    BlockStateConverter.Named SILVERFISH_AFFECTED = of("infestation");
    BlockStateConverter.Named FIRE_AFFECTED = of("fire");
    BlockStateConverter.Named BURNABLE = of("burnable");
    BlockStateConverter.Named HELLFIRE_AFFECTED = of("hellfire");

    private static BlockStateConverter.Named of(String name) {
        return BlockStateConverter.of(Unicopia.id(name));
    }
}
