package com.minelittlepony.unicopia.edibles;

import net.minecraft.client.resources.I18n;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;

public enum Toxicity {
    SAFE(0, 0),
    MILD(1, 160),
    FAIR(1, 30),
    SEVERE(5, 160),
    LETHAL(10, 900);

    private final int level;
    private final int duration;

    private static final Toxicity[] values = values();

    Toxicity(int level, int duration) {
        this.level = level;
        this.duration = duration;
    }

    public boolean isMild() {
        return this == MILD;
    }

    public boolean toxicWhenRaw() {
        return isLethal() || this != SAFE;
    }

    public boolean toxicWhenCooked() {
        return isLethal() || this == SEVERE;
    }

    public boolean isLethal() {
        return this == LETHAL;
    }

    public PotionEffect getPoisonEffect() {
        return new PotionEffect(isMild() ? MobEffects.NAUSEA : MobEffects.POISON, duration, level);
    }

    public String getTranslationKey() {
        return String.format("toxicity.%s.name", name().toLowerCase());
    }

    public String getTooltip() {
        TextFormatting color = toxicWhenCooked() ? TextFormatting.RED : toxicWhenRaw() ? TextFormatting.DARK_PURPLE : TextFormatting.GRAY;

        return color + I18n.format(getTranslationKey());
    }

    public static Toxicity byMetadata(int metadata) {
        return values[metadata % values.length];
    }

    public static String[] getVariants(String key) {
        String[] result = new String[values.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = values[i].name() + key;
        }

        return result;
    }
}
