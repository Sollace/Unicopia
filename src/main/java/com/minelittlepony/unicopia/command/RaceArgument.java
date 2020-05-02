package com.minelittlepony.unicopia.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Race;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

class RaceArgument implements ArgumentType<Race> {
    static final Collection<String> EXAMPLES = Arrays.stream(Race.values())
            .filter(Race::isUsable)
            .map(Race::name)
            .collect(Collectors.toList());

    @Override
    public Race parse(StringReader reader) throws CommandSyntaxException {
        return Race.fromName(reader.readUnquotedString(), Race.EARTH);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        Arrays.stream(Race.values())
            .filter(Race::isUsable)
            .map(i -> i.name().toLowerCase())
            .filter(i -> i.startsWith(builder.getRemaining().toLowerCase()))
            .forEach(i -> builder.suggest(i.toLowerCase()));

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}