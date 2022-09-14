package com.minelittlepony.unicopia.item.toxin;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public enum Toxicity {
    SAFE(Formatting.GRAY),
    MILD(Formatting.DARK_AQUA),
    FAIR(Formatting.DARK_BLUE),
    SEVERE(Formatting.DARK_PURPLE),
    LETHAL(Formatting.RED);

    private static final Map<String, Toxicity> REGISTRY = Arrays.stream(values()).collect(Collectors.toMap(Toxicity::name, Function.identity()));

    private final Formatting color;

    Toxicity(Formatting color) {
        this.color = color;
    }

    public String getTranslationKey() {
        return String.format("toxicity.%s.name", name().toLowerCase());
    }

    public Text getTooltip() {
        return new TranslatableText(getTranslationKey()).formatted(color);
    }

    public static Toxicity byName(String name) {
        return REGISTRY.get(name.toUpperCase());
    }
}
