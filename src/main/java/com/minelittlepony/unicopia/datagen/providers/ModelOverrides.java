package com.minelittlepony.unicopia.datagen.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;

public final class ModelOverrides {
    private final Model model;
    private final List<Override> overrides = new ArrayList<>();

    public static ModelOverrides of(Model model) {
        return new ModelOverrides(model);
    }

    private ModelOverrides(Model model) {
        this.model = model;
    }

    public <T> ModelOverrides addUniform(String key, Iterable<T> values, Function<T, Float> idFunc, Function<T, Identifier> childModelSupplier) {
        for (T t : values) {
            addOverride(childModelSupplier.apply(t), key, idFunc.apply(t));
        }
        return this;
    }

    public ModelOverrides addUniform(String key, int from, int to, Identifier model) {
        float step = 1F / to;
        for (int index = from; index <= to; index++) {
            addOverride(model.withSuffixedPath("_" + index), key, index * step);
        }
        return this;
    }

    public ModelOverrides addUniform(String key, float from, float to, float step, ModelVariantSupplier childModelSupplier) {
        int index = 0;
        for (float value = from; value <= to; value += step) {
            final int capture = index++;
            final float capture2 = value;
            addOverride(key, value, generator -> {
                return childModelSupplier.upload(capture, capture2);
            });
        }
        return this;
    }

    public ModelOverrides addOverride(Identifier modelId, String key, float value) {
        return addOverride(modelId, TextureMap.layer0(modelId), key, value);
    }

    public ModelOverrides addOverride(Identifier modelId, TextureMap textures, String key, float value) {
        return addOverride(key, value, generator -> model.upload(modelId, textures, generator.writer));
    }

    public ModelOverrides addOverride(String key, float value, Function<ItemModelGenerator, Identifier> generator) {
        return addOverride(Map.of(key, value), generator);
    }

    public ModelOverrides addOverride(Map<String, Float> predicate, Function<ItemModelGenerator, Identifier> generator) {
        overrides.add(new Override(predicate, generator));
        return this;
    }

    public Identifier upload(Item item, ItemModelGenerator generator) {
        return upload(item, "", generator);
    }

    public Identifier upload(Item item, String suffex, ItemModelGenerator generator) {
        return upload(ModelIds.getItemModelId(item), TextureMap.layer0(ModelIds.getItemSubModelId(item, suffex)), generator);
    }

    public Identifier upload(Identifier id, TextureMap textures, ItemModelGenerator generator) {
        List<Pair<Identifier, Map<String, Float>>> overrides = this.overrides.stream()
                .map(override -> new Pair<>(override.model().apply(generator), override.predicate()))
                .toList();

        return model.upload(id, textures, (a, jsonSupplier) -> {
            generator.writer.accept(a, () -> Util.make(jsonSupplier.get(), json -> {
                json.getAsJsonObject().add("overrides", Util.make(new JsonArray(), array -> {
                    overrides.forEach(override -> {
                        array.add(writeOverride(override.getLeft(), override.getRight(), new JsonObject()));
                    });
                }));
            }));
        });
    }

    private JsonObject writeOverride(Identifier model, Map<String, Float> predicate, JsonObject json) {
        json.addProperty("model", model.toString());
        json.add("predicate", Util.make(new JsonObject(), output -> {
            predicate.forEach(output::addProperty);
        }));
        return json;
    }

    private record Override(Map<String, Float> predicate, Function<ItemModelGenerator, Identifier> model) {

    }

    public interface ModelVariantSupplier {
        Identifier upload(int index, float value);
    }
}
