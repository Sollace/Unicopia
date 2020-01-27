package com.minelittlepony.unicopia.magic;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public enum Affinity {
    GOOD(Formatting.BLUE, -1),
    NEUTRAL(Formatting.WHITE, 0),
    BAD(Formatting.RED, 1);

    private final Formatting color;

    private final int corruption;

    private Affinity[] implications;

    Affinity(Formatting color, int corruption) {
        this.color = color;
        this.corruption = corruption;
    }

    public Formatting getColourCode() {
        return color;
    }

    public String getTranslationKey() {
        return this == BAD ? "curse" : "spell";
    }

    public Text getName() {
        Text text = new TranslatableText("affinity." + getTranslationKey() + ".name");
        text.getStyle().setColor(getColourCode());
        return text;
    }

    public int getCorruption() {
        return corruption;
    }

    public boolean isNeutral() {
        return this == NEUTRAL;
    }

    public Affinity[] getImplicators() {
        if (implications != null) {
            return implications;
        }

        if (this == NEUTRAL) {
            implications = values();
        } else {
            implications = new Affinity[] { this };
        }

        return implications;
    }

    public static Affinity of(String s) {
        try {
            if (s != null)
                return valueOf(s.toUpperCase());
        } catch (Throwable e) {}

        return Affinity.NEUTRAL;
    }
}
