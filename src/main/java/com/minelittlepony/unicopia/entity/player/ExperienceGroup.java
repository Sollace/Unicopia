package com.minelittlepony.unicopia.entity.player;

import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public enum ExperienceGroup {
    MAGICAL_KINDERGARTENER,
    FRIENDSHIP_STUDENT,
    SENIOR_FRIENDSHIP_STUDENT,
    JUNIOR_MAGE,
    MAGE,
    ARCHMAGE,
    ARCHMAGUS,
    SENIOR_ARCHMAGUS,
    ASCENDED_SENIOR_ARCHMAGUS,
    DEMI_GOD,
    ARCH_DEMI_GOD,
    ALICORN_PRINCESS,
    POLYCORN_PRINCESS,
    FAUSTIAN_LEGEND;

    private final Text label = Text.literal(name().toLowerCase());

    public Text getLabel() {
        return label;
    }

    public static ExperienceGroup forLevel(int level) {
        level /= 20;
        level = MathHelper.clamp(level, 0, values().length - 1);
        return values()[level];
    }
}
