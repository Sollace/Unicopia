package com.minelittlepony.unicopia.datagen.providers;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.util.Identifier;

public class SeasonsModelGenerator extends UBlockStateModelGenerator {

    private static final String[] SEASONS = { "fall", "summer", "winter" };

    public SeasonsModelGenerator(BlockStateModelGenerator modelGenerator, BiConsumer<Identifier, Supplier<JsonElement>> seasonsModelConsumer) {
        super(modelGenerator.blockStateCollector, (id, jsonSupplier) -> {
            modelGenerator.modelCollector.accept(id, jsonSupplier);
            seasonsModelConsumer.accept(id, () -> {
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

    @Override
    public void register() {
        registerWithStages(UBlocks.OATS, UBlocks.OATS.getAgeProperty(), BlockModels.CROP, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        registerWithStages(UBlocks.OATS_STEM, UBlocks.OATS_STEM.getAgeProperty(), BlockModels.CROP, 0, 1, 2, 3, 4, 5, 6);
        registerWithStages(UBlocks.OATS_CROWN, UBlocks.OATS_CROWN.getAgeProperty(), BlockModels.CROP, 0, 1);

        registerItemModel(UItems.OATS);
        registerItemModel(UItems.OAT_SEEDS);
    }
}
