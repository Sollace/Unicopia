package com.minelittlepony.unicopia.container;

import net.minecraft.text.Text;

public enum SpellbookPage {
    INVENTORY,
    RECIPES;

    public static final SpellbookPage[] VALUES = values();

    private final Text label = Text.translatable("gui.unicopia.spellbook.page." + name().toLowerCase());

    public Text getLabel() {
        return label;
    }
}
