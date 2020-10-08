package com.minelittlepony.unicopia.item.toxin;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final Map<String, Toxicity> REGISTRY = Arrays.stream(values()).collect(Collectors.toMap(Toxicity::name, Function.identity()));

    Toxicity(int level, int duration) {
        this.level = level;
        this.duration = duration;
    }

    public int getLevel() {
        return level;
    }

    public int getDuration() {
        return duration;
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

    public String getTranslationKey() {
        return String.format("toxicity.%s.name", name().toLowerCase());
    }

    public Text getTooltip() {
        return new TranslatableText(getTranslationKey())
                .styled(s -> s
                        .withColor(toxicWhenCooked() ? Formatting.RED : toxicWhenRaw() ? Formatting.DARK_PURPLE : Formatting.GRAY));
    }

    public static Toxicity byName(String name) {
        return REGISTRY.get(name.toUpperCase());
    }
}
