package com.minelittlepony.unicopia.edibles;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

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

    public Text getTooltip() {
        Text text = new TranslatableText(getTranslationKey());
        text.getStyle().setColor(toxicWhenCooked() ? Formatting.RED : toxicWhenRaw() ? Formatting.DARK_PURPLE : Formatting.GRAY);
        return text;
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
