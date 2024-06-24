package com.minelittlepony.unicopia.ability.magic.spell.attribute;

public enum Affects {
    BOTH,
    ENTITIES,
    BLOCKS;

    public boolean allowsBlocks() {
        return this == BOTH || this == BLOCKS;
    }

    public boolean allowsEntities() {
        return this == BOTH || this == ENTITIES;
    }
}
