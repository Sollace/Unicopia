package com.minelittlepony.unicopia.datagen.importers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class ResourceCollector {
    private final Path rootPath;

    public ResourceCollector(Path rootPath, ResourceType type) {
        this.rootPath = rootPath.resolve(type.getDirectory());
    }

    public <T> Stream<Map.Entry<Identifier, T>> collect(RegistryWrapper.WrapperLookup registries, Codec<T> codec, String assetPath) {
        try {
            return Files.list(rootPath)
                    .flatMap(domainPath -> {
                        try {
                            Path assetFolder = domainPath.resolve(assetPath);
                            if (!Files.isDirectory(assetFolder)) {
                                return Stream.empty();
                            }
                            return Files.walk(assetFolder).filter(path -> path.toString().endsWith(".json")).map(path -> {
                                String[] parts = path.toString().replace(rootPath.toString(), "").split(assetPath);
                                Identifier id = Identifier.of(parts[0].replaceAll("^/|/$", ""), parts[1].split("\\.")[0].replaceAll("^/|/$", ""));

                                try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
                                    JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();

                                    T value = codec.decode(registries.getOps(JsonOps.INSTANCE), data)
                                            .ifError(error -> Datagen.LOGGER.error("Cannot decode entry {}: {}", id, error))
                                            .result().map(pair -> pair.getFirst()).orElse(null);
                                    return value == null ? null : Map.entry(id, value);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }).filter(Objects::nonNull);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
