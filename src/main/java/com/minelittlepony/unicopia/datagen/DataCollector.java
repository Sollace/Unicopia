package com.minelittlepony.unicopia.datagen;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.data.DataOutput.PathResolver;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

public class DataCollector<T> {
    private final HashMap<Identifier, T> values = new HashMap<>();

    private final PathResolver resolver;
    private final Function<T, JsonElement> jsonConversionFunction;

    public DataCollector(PathResolver resolver, Function<T, JsonElement> jsonConversionFunction) {
        this.resolver = resolver;
        this.jsonConversionFunction = jsonConversionFunction;
    }

    public DataCollector(PathResolver resolver, Codec<T> codec) {
        this(resolver, t -> codec.encodeStart(JsonOps.INSTANCE, t).getOrThrow());
    }

    public boolean isDefined(Identifier id) {
        return values.containsKey(id);
    }

    public <V> Consumer<V> prime(BiConsumer<V, BiConsumer<Identifier, T>> converter) {
        var consumer = prime();
        return element -> converter.accept(element, consumer);
    }

    public BiConsumer<Identifier, T> prime() {
        values.clear();
        return (Identifier id, T value) ->
            Preconditions.checkState(values.put(id, value) == null, "Duplicate model definition for " + id);
    }

    public CompletableFuture<?> upload(DataWriter cache) {
        return CompletableFuture.allOf(values.entrySet()
                .stream()
                .map(entry -> DataProvider.writeToPath(cache, jsonConversionFunction.apply(entry.getValue()), resolver.resolveJson(entry.getKey())))
                .toArray(CompletableFuture[]::new)
        );
    }

    public interface Identifiable {
        Identifier getId();
    }
}
