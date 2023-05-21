package com.minelittlepony.unicopia.entity.player;

import java.lang.ref.WeakReference;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public interface ExperienceGroup {
    String[] EXPERIENCES = {
            "magical_kindergartner",
            "friendship_student",
            "senior_friendship_student",
            "junior_mage",
            "mage",
            "archmage",
            "archmagus",
            "senior_archmagus",
            "ascended_senior_archmagus",
            "demi_god",
            "arch_demi_god",
            "alicorn_princess",
            "polycorn_princess",
            "faustian_legend"
    };
    String[] CORRUPTIONS = {
            "pure",
            "impure",
            "tainted",
            "twisted",
            "corrupt",
            "monstrous"
    };
    Int2ObjectArrayMap<WeakReference<Text>> CACHE = new Int2ObjectArrayMap<>();

    static Text forLevel(float level, float corruption) {
        int c = get(CORRUPTIONS, corruption);
        int x = get(EXPERIENCES, level);
        int i = x * c;

        Text value;
        if (!CACHE.containsKey(i) || (value = CACHE.get(i).get()) == null) {
            CACHE.put(i, new WeakReference<>(value = Text.translatable("experience.unicopia." + CORRUPTIONS[c] + "." + EXPERIENCES[x])));
        }
        return value;
    }

    private static int get(String[] values, float level) {
        return MathHelper.clamp(MathHelper.floor(level * values.length), 0, values.length - 1);
    }
}
