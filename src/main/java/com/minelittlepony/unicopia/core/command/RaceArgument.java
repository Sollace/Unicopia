package com.minelittlepony.unicopia.core.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.core.Race;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

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
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}