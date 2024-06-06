package com.minelittlepony.unicopia.ability.magic.spell.attribute;

import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;

import it.unimi.dsi.fastutil.floats.Float2ObjectFunction;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record SpellAttribute<T> (
        Trait trait,
        BiFunction<SpellTraits, Float, T> valueGetter,
        TooltipFactory tooltipFactory
) implements TooltipFactory {
    @Override
    public void appendTooltip(CustomisedSpellType<?> type, List<Text> tooltip) {
        tooltipFactory.appendTooltip(type, tooltip);
    }

    public T get(SpellTraits traits) {
        return valueGetter.apply(traits, traits.get(trait));
    }

    public static <T extends Number> SpellAttribute<T> create(Identifier id, AttributeFormat format, Trait trait, BiFunction<SpellTraits, Float, @NotNull T> valueGetter) {
        return create(id, format, format, trait, valueGetter, false);
    }

    public static <T extends Number> SpellAttribute<T> create(Identifier id, AttributeFormat format, Trait trait, Float2ObjectFunction<@NotNull T> valueGetter) {
        return create(id, format, format, trait, valueGetter, false);
    }

    public static <T extends Number> SpellAttribute<T> create(Identifier id, AttributeFormat baseFormat, AttributeFormat relativeFormat, Trait trait, Float2ObjectFunction<@NotNull T> valueGetter) {
        return create(id, baseFormat, relativeFormat, trait, valueGetter, false);
    }

    public static <T extends Number> SpellAttribute<T> create(Identifier id, AttributeFormat baseFormat, AttributeFormat relativeFormat, Trait trait, BiFunction<SpellTraits, Float, @NotNull T> valueGetter) {
        return create(id, baseFormat, relativeFormat, trait, valueGetter, false);
    }

    public static <T extends @NotNull Number> SpellAttribute<T> create(Identifier id, AttributeFormat baseFormat, AttributeFormat relativeFormat, Trait trait, Float2ObjectFunction<@NotNull T> valueGetter, boolean detrimental) {
        return create(id, baseFormat, relativeFormat, trait, (traits, value) -> valueGetter.get(value.floatValue()), detrimental);
    }

    public static <T extends @NotNull Number> SpellAttribute<T> create(Identifier id, AttributeFormat baseFormat, AttributeFormat relativeFormat, Trait trait, BiFunction<SpellTraits, Float, @NotNull T> valueGetter, boolean detrimental) {
        Text name = Text.translatable(Util.createTranslationKey("spell_attribute", id));
        return new SpellAttribute<>(trait, valueGetter, (CustomisedSpellType<?> type, List<Text> tooltip) -> {
            float traitAmount = type.traits().get(trait);
            float traitDifference = type.relativeTraits().get(trait);
            float value = valueGetter.apply(type.traits(), traitAmount).floatValue();

            var b = baseFormat.getBase(name, value, "equals", Formatting.LIGHT_PURPLE);
            if (traitDifference != 0) {
                tooltip.add(b.append(relativeFormat.getRelative(Text.empty(), valueGetter.apply(type.traits(), traitAmount - traitDifference).floatValue(), value, detrimental)));
                tooltip.add(AttributeFormat.formatTraitDifference(trait, traitDifference));
            } else {
                tooltip.add(b);
            }
        });
    }

    public static SpellAttribute<Boolean> createConditional(Identifier id, Trait trait, Float2ObjectFunction<Boolean> valueGetter) {
        return createConditional(id, trait, (traits, value) -> valueGetter.get(value.floatValue()));
    }

    public static SpellAttribute<Boolean> createConditional(Identifier id, Trait trait, BiFunction<SpellTraits, Float, @NotNull Boolean> valueGetter) {
        return new SpellAttribute<>(trait, valueGetter, (CustomisedSpellType<?> type, List<Text> tooltip) -> {
            Text name = Text.translatable(Util.createTranslationKey("spell_attribute", id));
            float difference = type.relativeTraits().get(trait);
            Text value = AttributeFormat.formatAttributeLine(name);
            if (!valueGetter.apply(type.traits(), type.traits().get(trait))) {
                value = value.copy().formatted(Formatting.STRIKETHROUGH, Formatting.DARK_GRAY);
            }
            tooltip.add(value);
            if (difference != 0) {
                tooltip.add(AttributeFormat.formatTraitDifference(trait, difference));
            }
        });
    }

    public static <T extends Enum<T>> SpellAttribute<T> createEnumerated(Identifier id, Trait trait, Float2ObjectFunction<T> valueGetter) {
        return createEnumerated(id, trait, (traits, value) -> valueGetter.get(value.floatValue()));
    }

    public static <T extends Enum<T>> SpellAttribute<T> createEnumerated(Identifier id, Trait trait, BiFunction<SpellTraits, Float, @NotNull T> valueGetter) {
        Function<T, Text> cache = Util.memoize(t -> Text.translatable(Util.createTranslationKey("spell_attribute", id.withPath(id.getPath() + "." + t.name().toLowerCase(Locale.ROOT)))));
        return new SpellAttribute<>(trait, valueGetter, (CustomisedSpellType<?> type, List<Text> tooltip) -> {
            T t = valueGetter.apply(type.traits(), type.traits().get(trait));

            if (t != null) {
                int max = t.getClass().getEnumConstants().length;
                tooltip.add(Text.translatable(" %s (%s/%s)", cache.apply(t), Text.literal("" + (t.ordinal() + 1)).formatted(Formatting.LIGHT_PURPLE), max).formatted(Formatting.DARK_PURPLE));
            }
            float difference = type.relativeTraits().get(trait);
            if (difference != 0) {
                tooltip.add(AttributeFormat.formatTraitDifference(trait, difference));
            }
        });
    }
}
