package com.minelittlepony.unicopia.item.toxin;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

public enum Toxicity implements StringIdentifiable {
    SAFE(Formatting.GRAY),
    MILD(Formatting.DARK_AQUA),
    FAIR(Formatting.DARK_BLUE),
    SEVERE(Formatting.DARK_PURPLE),
    LETHAL(Formatting.RED);

    private static final Map<String, Toxicity> REGISTRY = Arrays.stream(values()).collect(Collectors.toMap(Toxicity::name, Function.identity()));
    @SuppressWarnings("deprecation")
    public static final Codec<Toxicity> CODEC = StringIdentifiable.createCodec(Toxicity::values);

    private final Formatting color;
    private final String name = name().toLowerCase(Locale.ROOT);

    Toxicity(Formatting color) {
        this.color = color;
    }

    public String getTranslationKey() {
        return String.format("toxicity.%s.name", name().toLowerCase());
    }

    public Text getTooltip() {
        return Text.translatable(getTranslationKey()).formatted(color);
    }

    public static Toxicity byName(String name) {
        return REGISTRY.get(name.toUpperCase());
    }

    @Override
    public String asString() {
        return name;
    }
}
