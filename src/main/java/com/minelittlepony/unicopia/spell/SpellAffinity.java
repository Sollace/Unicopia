package com.minelittlepony.unicopia.spell;

import net.minecraft.util.text.TextFormatting;

public enum SpellAffinity {
    GOOD(TextFormatting.BLUE, -1),
    NEUTRAL(TextFormatting.WHITE, 0),
    BAD(TextFormatting.RED, 1);

    private final TextFormatting color;

    private final int corruption;

    private SpellAffinity[] implications;

    SpellAffinity(TextFormatting color, int corruption) {
        this.color = color;
        this.corruption = corruption;
    }

    public TextFormatting getColourCode() {
        return color;
    }

    public String getTranslationKey() {
        return this == BAD ? "curse" : "spell";
    }

    public int getCorruption() {
        return corruption;
    }

    public boolean isNeutral() {
        return this == NEUTRAL;
    }

    public SpellAffinity[] getImplicators() {
        if (implications != null) {
            return implications;
        }

        if (this == NEUTRAL) {
            implications = values();
        } else {
            implications = new SpellAffinity[] { this };
        }

        return implications;
    }
}
