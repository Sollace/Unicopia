package com.minelittlepony.unicopia.container.inventory;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.util.Identifier;

public interface SpellbookSlot {
    Identifier EMPTY_TEXTURE = Unicopia.id("transparent");
    Identifier GEM = Unicopia.id("textures/item/gemstone.png");

    float CENTER_FACTOR = 0;
    float NEAR_FACTOR = 1;
    float MIDDLE_FACTOR = 0.6F;
    float FAR_FACTOR = 0.3F;

    default float getWeight() {
        return CENTER_FACTOR;
    }

    default float getBackSpriteOpacity() {
        return 0.3F;
    }

    @Nullable
    default Identifier getForegroundIdentifier() {
        return null;
    }

    default boolean isTrinket() {
        return false;
    }

    default boolean showTraits() {
        return true;
    }
}