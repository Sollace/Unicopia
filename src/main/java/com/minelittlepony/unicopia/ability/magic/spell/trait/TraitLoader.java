package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Resources;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

public class TraitLoader extends SinglePreparationResourceReloader<Multimap<Identifier, TraitLoader.TraitStream>> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/traits");

    public static final TraitLoader INSTANCE = new TraitLoader();

    private Map<Identifier, SpellTraits> values = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    public SpellTraits getTraits(Item item) {
        return values.getOrDefault(Registry.ITEM.getId(item), SpellTraits.EMPTY);
    }

    @Override
    protected Multimap<Identifier, TraitStream> prepare(ResourceManager manager, Profiler profiler) {
        profiler.startTick();

        Multimap<Identifier, TraitStream> prepared = HashMultimap.create();

        for (var path : manager.findResources("traits", p -> p.getPath().endsWith(".json")).keySet()) {
            profiler.push(path.toString());
            try {
                for (Resource resource : manager.getAllResources(path)) {
                    profiler.push(resource.getResourcePackName());

                    try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                        JsonObject data = JsonHelper.deserialize(Resources.GSON, reader, JsonObject.class);

                        TraitStream set = TraitStream.of(path, resource.getResourcePackName(), data);

                        if (set.replace()) {
                            prepared.removeAll(path);
                        }
                        prepared.put(path, set);
                    } catch (JsonParseException e) {
                        Unicopia.LOGGER.error("Error reading traits file " + resource.getResourcePackName() + ":" + path, e);
                    } finally {
                        profiler.pop();
                    }
                }
            } catch (IOException e) {
                Unicopia.LOGGER.error("Error reading traits file " + path, e);
            } finally {
                profiler.pop();
            }
        }

        profiler.endTick();
        return prepared;
    }

    @Override
    protected void apply(Multimap<Identifier, TraitStream> prepared, ResourceManager manager, Profiler profiler) {
        profiler.startTick();
        values = prepared.values().stream()
                .flatMap(TraitStream::entries)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, SpellTraits::union));
        profiler.endTick();
    }

    interface TraitStream {
        TypeToken<Map<String, String>> TYPE = new TypeToken<>() {};

        boolean replace();

        Stream<Map.Entry<Identifier, SpellTraits>> entries();

        static TraitStream of(Identifier id, String pack, JsonObject json) {

            if (json.has("items") && json.get("items").isJsonObject()) {
                return new TraitMap(JsonHelper.getBoolean(json, "replace", false),
                        Resources.GSON.getAdapter(TYPE).fromJsonTree(json.get("items")).entrySet().stream().collect(Collectors.toMap(
                                a -> Identifier.tryParse(a.getKey()),
                                a -> SpellTraits.fromString(a.getValue()).orElse(SpellTraits.EMPTY)
                        ))
                );
            }

            return new TraitSet(
                    JsonHelper.getBoolean(json, "replace", false),
                    SpellTraits.fromString(JsonHelper.getString(json, "traits")).orElse(SpellTraits.EMPTY),
                    StreamSupport.stream(JsonHelper.getArray(json, "items").spliterator(), false)
                        .map(JsonElement::getAsString)
                        .map(Identifier::tryParse)
                        .filter(item -> {
                            if (item == null || !Registry.ITEM.containsId(item)) {
                                Unicopia.LOGGER.warn("Skipping unknown item {} in {}:{}", item, pack, id);
                                return false;
                            }
                            return true;
                        })
                        .collect(Collectors.toSet())
            );
        }

        record TraitMap (
                boolean replace,
                Map<Identifier, SpellTraits> items) implements TraitStream {
            @Override
            public Stream<Entry<Identifier, SpellTraits>> entries() {
                return items.entrySet().stream();
            }
        }

        record TraitSet (
                boolean replace,
                SpellTraits traits,
                Set<Identifier> items) implements TraitStream {
            @Override
            public Stream<Entry<Identifier, SpellTraits>> entries() {
                return items().stream().map(item -> Map.entry(item, traits()));
            }
        }
    }

}
