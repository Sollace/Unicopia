package com.minelittlepony.unicopia.item.toxin;

public enum FoodType {
    RAW_MEAT, COOKED_MEAT,
    RAW_FISH, COOKED_FISH,
    FORAGE;

    public boolean isRaw() {
        return this == RAW_MEAT || this == RAW_FISH || this == FORAGE;
    }

    public boolean isMeat() {
        return this == RAW_MEAT || this == COOKED_MEAT;
    }

    public boolean isFish() {
        return this == RAW_FISH || this == COOKED_FISH;
    }
}
