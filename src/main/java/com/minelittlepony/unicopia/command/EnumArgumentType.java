package com.minelittlepony.unicopia.command;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ArgumentSerializer.ArgumentTypeProperties;
import net.minecraft.network.PacketByteBuf;

class EnumArgumentType<T extends Enum<T>> implements ArgumentType<T>, Serializable {
    private static final long serialVersionUID = 3731493854867412243L;

    public static <T extends Enum<T>> EnumArgumentType<T> of(Class<T> type, Predicate<T> filter, T def) {
        return new EnumArgumentType<>(type, filter, def);
    }

    public static <T extends Enum<T>> EnumArgumentType<T> of(Class<T> type, T def) {
        return new EnumArgumentType<>(type, s -> true, def);
    }

    public static <T extends Enum<T>> EnumArgumentType<T> of(Class<T> type) {
        return new EnumArgumentType<>(type, s -> true, null);
    }

    private final T def;
    private final T[] values;
    private final List<String> suggestions;

    private EnumArgumentType(Class<T> type, Predicate<T> filter, T def) {
        this.def = def;
        values = type.getEnumConstants();
        suggestions = Arrays.stream(values).filter(filter).map(T::name).toList();
    }

    private EnumArgumentType(List<String> suggestions, T[] values, T def) {
        this.suggestions = suggestions;
        this.values = values;
        this.def = def;
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        return fromName(reader.readUnquotedString());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        suggestions.stream()
            .filter(i -> i.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    @SuppressWarnings("unlikely-arg-type")
    private T fromName(String s) {
        if (!Strings.isNullOrEmpty(s)) {
            for (T i : values) {
                if (i.equals(s) || i.name().equalsIgnoreCase(s)) {
                    return i;
                }
            }
        }

        try {
            int ordinal = Integer.parseInt(s);
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
        } catch (NumberFormatException e) { }

        return def;
    }

    @Override
    public Collection<String> getExamples() {
        return suggestions;
    }

    public static class Serializer<T extends Enum<T>> implements ArgumentSerializer<EnumArgumentType<T>, Serializer<T>.Properties> {
        @SuppressWarnings("unchecked")
        @Override
        public Properties fromPacket(PacketByteBuf buf) {
            try (ObjectInputStream stream = new ObjectInputStream(new ByteBufInputStream(buf))) {
                return getArgumentTypeProperties((EnumArgumentType<T>)stream.readObject());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void writePacket(Properties properties, PacketByteBuf buf) {
            try (ObjectOutputStream stream = new ObjectOutputStream(new ByteBufOutputStream(buf))) {
                stream.writeObject(properties.type);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void writeJson(Properties properties, JsonObject json) {
        }

        @Override
        public Properties getArgumentTypeProperties(EnumArgumentType<T> type) {
            return new Properties(type);
        }

        public final class Properties implements ArgumentTypeProperties<EnumArgumentType<T>> {

            private final EnumArgumentType<T> type;

            public Properties(EnumArgumentType<T> type) {
                this.type = type;
            }

            @Override
            public EnumArgumentType<T> createType(CommandRegistryAccess var1) {
                return type;
            }

            @Override
            public ArgumentSerializer<EnumArgumentType<T>, ?> getSerializer() {
                return Serializer.this;
            }
        }
    }
}