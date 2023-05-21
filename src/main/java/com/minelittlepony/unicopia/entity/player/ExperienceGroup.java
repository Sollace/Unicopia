package com.minelittlepony.unicopia.entity.player;

import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public interface ExperienceGroup {
    LinearSelector<String> EXPERIENCES = new LinearSelector<>(new String[] {
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
    LinearSelector<String> CORRUPTIONS = new LinearSelector<>(new String[] {
            "PURE",
            "IMPURE",
            "TAINTED",
            "TWISTED",
            "CORRUPT",
            "MONSTROUS"
    }, 1F/8F);

    static Text forLevel(float level, float corruption) {
        return Text.translatable(
            "experience.unicopia." + CORRUPTIONS.get(corruption).toLowerCase() + "." + EXPERIENCES.get(level).toLowerCase()
        );
    }

    public record LinearSelector<T> (T[] values, float ratio) {
        public T get(float level) {
            return values()[MathHelper.clamp(MathHelper.floor(level * values().length), 0, values().length - 1)];
        }
    }
}
