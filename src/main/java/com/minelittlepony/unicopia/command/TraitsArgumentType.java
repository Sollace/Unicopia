package com.minelittlepony.unicopia.command;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.text.Text;

class TraitsArgumentType implements ArgumentType<SpellTraits> {
    private static final List<String> EXAMPLES = List.of("strength:1,focus:2", "");
    public static final SimpleCommandExceptionType UNRECOGNISED_TRAIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.spell_trait.unrecognised"));

    private TraitsArgumentType() {

    }

    public static TraitsArgumentType traits() {
        return new TraitsArgumentType();
    }

    public static <S> SpellTraits getSpellTraits(CommandContext<S> context, String name) {
        return context.getArgument(name, SpellTraits.class);
    }

    @Override
    public SpellTraits parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            return SpellTraits.EMPTY;
        }

        SpellTraits.Builder builder = new SpellTraits.Builder();
        while (reader.canRead() && reader.peek() != ' ') {
            Trait trait = Trait.fromName(readTraitName(reader)).orElseThrow(() -> UNRECOGNISED_TRAIT_EXCEPTION.createWithContext(reader));
            reader.expect(':');
            float value = reader.readFloat();
            builder.with(trait, value);
            if (!reader.canRead() || reader.peek() != ',') {
                break;
            }
            reader.skip();
        }

        return builder.build();
    }

    private String readTraitName(StringReader reader) {
        StringBuilder builder = new StringBuilder();
        while (reader.canRead() && reader.peek() != ' ' && reader.peek() != ':') {
            builder.append(reader.read());
        }

        return builder.toString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(Locale.ROOT);
        if (input.indexOf(' ') != -1) {
            return Suggestions.empty();
        }

        int colonIndex = input.lastIndexOf(':');
        int commaIndex = Math.max(input.lastIndexOf(','), colonIndex);
        if (commaIndex > -1) {
            builder = builder.createOffset(builder.getStart() + commaIndex + 1);
            input = input.substring(commaIndex + 1, input.length());

            if (commaIndex == colonIndex) {
                return Suggestions.empty();
            }
        }

        final String incomplete = input;
        System.out.println(incomplete);
        Trait.all().stream()
            .map(trait -> trait.name().toLowerCase(Locale.ROOT))
            .filter(trait -> incomplete.isBlank() || trait.startsWith(incomplete))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public String toString() {
        return "SpellTraits()";
    }
}