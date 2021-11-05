package com.minelittlepony.unicopia.ability.magic.spell.trait;

public enum TraitGroup {
    NATURE(-0.2F),
    DARKNESS(0.4F),
    ELEMENTAL(-0.02F);

    private final float corruption;

    TraitGroup(float corruption) {
        this.corruption = corruption;
    }

    // TODO: implement corruption mechanics
    public float getCorruption() {
        return corruption;
    }
}
