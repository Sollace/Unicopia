package com.minelittlepony.unicopia.datagen.providers;

import com.google.gson.JsonObject;

import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.util.Identifier;

class SeasonsModelGenerator {
    private static final String[] SEASONS = { "fall", "summer", "winter" };

    static UBlockStateModelGenerator create(BlockStateModelGenerator modelGenerator) {
        return new UBlockStateModelGenerator(modelGenerator.blockStateCollector, (id, jsonSupplier) -> {
            modelGenerator.modelCollector.accept(id, jsonSupplier);
            modelGenerator.modelCollector.accept(id.withPrefixedPath("seasons/"), () -> {
                JsonObject textures = jsonSupplier.get().getAsJsonObject().getAsJsonObject("textures");
                JsonObject seasonTextures = new JsonObject();
                for (String season : SEASONS) {
                    seasonTextures.add(season, createTextures(season, textures));
                }
                JsonObject model = new JsonObject();
                model.add("textures", seasonTextures);
                return model;
            });
        }, modelGenerator::excludeFromSimpleItemModelGeneration);
    }

    private static JsonObject createTextures(String season, JsonObject input) {
        JsonObject textures = new JsonObject();
        input.entrySet().forEach(entry -> {
            textures.addProperty(entry.getKey(), new Identifier(entry.getValue().getAsString()).withPath(path -> path.replace("/", "/seasons/" + season + "/")).toString());
        });
        return textures;
    }
}
