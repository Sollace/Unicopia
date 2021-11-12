package com.minelittlepony.unicopia.ability.magic.spell.trait;

public enum TraitGroup {
    NATURAL(-0.2F),
    DARKNESS(0.4F),
    ELEMENTAL(-0.02F),
    MAGICAL(-0.1F);

    private final float corruption;

    TraitGroup(float corruption) {
        this.corruption = corruption;
    }

    // TODO: implement corruption mechanics
    public float getCorruption() {
        return corruption;
    }
}
