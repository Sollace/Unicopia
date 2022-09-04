package com.minelittlepony.unicopia.entity.player;

import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public record ExperienceGroup (String experience, String corruption) {
    public static final LinearSelector<String> EXPERIENCES = new LinearSelector<>(new String[] {
            "MAGICAL_KINDERGARTENER",
            "FRIENDSHIP_STUDENT",
            "SENIOR_FRIENDSHIP_STUDENT",
            "JUNIOR_MAGE",
            "MAGE",
            "ARCHMAGE",
            "ARCHMAGUS",
            "SENIOR_ARCHMAGUS",
            "ASCENDED_SENIOR_ARCHMAGUS",
            "DEMI_GOD",
            "ARCH_DEMI_GOD",
            "ALICORN_PRINCESS",
            "POLYCORN_PRINCESS",
            "FAUSTIAN_LEGEND"
    }, 2);
    public static final LinearSelector<String> CORRUPTIONS = new LinearSelector<>(new String[] {
            "PURE",
            "IMPURE",
            "TAINTED",
            "TWISTED",
            "CORRUPT",
            "MONSTROUS"
    }, 1F/8F);

    public static Text forLevel(int level, int corruption) {
        return Text.of(CORRUPTIONS.get(corruption).toLowerCase() + " " + EXPERIENCES.get(level).toLowerCase());
    }

    public record LinearSelector<T> (T[] values, float ratio) {
        public T get(int level) {
            return values()[MathHelper.clamp(MathHelper.floor(MathHelper.sqrt(level * ratio())), 0, values().length - 1)];
        }
    }
}
