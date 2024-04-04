package com.minelittlepony.unicopia.datagen;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;

import net.minecraft.data.DataOutput.PathResolver;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

public class DataCollector {
    private final HashMap<Identifier, Supplier<JsonElement>> values = new HashMap<>();

    private final PathResolver resolver;

    public DataCollector(PathResolver resolver) {
        this.resolver = resolver;
    }

    public boolean isDefined(Identifier id) {
        return values.containsKey(id);
    }

    public BiConsumer<Identifier, Supplier<JsonElement>> prime() {
        values.clear();
        return (Identifier id, Supplier<JsonElement> value) ->
            Preconditions.checkState(values.put(id, value) == null, "Duplicate model definition for " + id);
    }

    public CompletableFuture<?> upload(DataWriter cache) {
        return CompletableFuture.allOf(values.entrySet()
                .stream()
                .map(entry -> DataProvider.writeToPath(cache, entry.getValue().get(), resolver.resolveJson(entry.getKey())))
                .toArray(CompletableFuture[]::new)
        );
    }
}
