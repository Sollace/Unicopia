package com.minelittlepony.unicopia.container.inventory;

public interface SpellbookSlot {
    float CENTER_FACTOR = 0;
    float NEAR_FACTOR = 1;
    float MIDDLE_FACTOR = 0.6F;
    float FAR_FACTOR = 0.3F;

    default float getWeight() {
        return CENTER_FACTOR;
    }
}