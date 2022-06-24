package com.minelittlepony.unicopia.container;

import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public enum SpellbookPage {
    INVENTORY,
    DISCOVERIES,
    RECIPES;

    public static final SpellbookPage[] VALUES = values();
    private static int current;

    private final Text label = Text.translatable("gui.unicopia.spellbook.page." + name().toLowerCase());

    public Text getLabel() {
        return label;
    }

    public boolean isFirst() {
        return ordinal() == 0;
    }

    public boolean isLast() {
        return ordinal() == VALUES.length - 1;
    }

    public static SpellbookPage getCurrent() {
        return VALUES[current];
    }

    public static void swap(int increment) {
        current = MathHelper.clamp(current + increment, 0, VALUES.length - 1);
    }
}
