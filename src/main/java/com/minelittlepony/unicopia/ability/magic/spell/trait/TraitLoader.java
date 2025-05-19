package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
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
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class TraitLoader extends SinglePreparationResourceReloader<Multimap<Identifier, TraitLoader.TraitStream>> implements IdentifiableResourceReloadListener {
    private static final Identifier ID = Unicopia.id("data/traits");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected Multimap<Identifier, TraitStream> prepare(ResourceManager manager, Profiler profiler) {
        profiler.startTick();

        Multimap<Identifier, TraitStream> prepared = HashMultimap.create();

        for (var path : manager.findResources("traits", p -> p.getPath().endsWith(".json")).keySet()) {
            profiler.push(path.toString());
            try {
                for (Resource resource : manager.getAllResources(path)) {
                    profiler.push(resource.getPackId());

                    try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                        JsonObject data = JsonHelper.deserialize(Resources.GSON, reader, JsonObject.class);

                        TraitStream set = TraitStream.of(path, resource.getPackId(), data);

                        if (set.replace()) {
                            prepared.removeAll(path);
                        }
                        prepared.put(path, set);
                    } catch (JsonParseException e) {
                        Unicopia.LOGGER.error("Error reading traits file " + resource.getPackId() + ":" + path, e);
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

        Set<Map.Entry<TraitStream.Key, SpellTraits>> newRegistry = prepared.values().stream()
                .flatMap(TraitStream::entries)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, SpellTraits::union))
                .entrySet();

        SpellTraits.load(Registries.ITEM.getEntrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey().getValue(),
                        newRegistry.stream()
                            .filter(p -> p.getKey().test(entry.getValue()))
                            .map(Map.Entry::getValue)
                            .reduce(SpellTraits::union)
                            .orElse(SpellTraits.EMPTY)
                ))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        profiler.endTick();
    }

    interface TraitStream {
        TypeToken<Map<String, String>> TYPE = new TypeToken<>() {};

        boolean replace();

        Stream<Map.Entry<Key, SpellTraits>> entries();

        static TraitStream of(Identifier id, String pack, JsonObject json) {

            if (json.has("items") && json.get("items").isJsonObject()) {
                return new TraitMap(JsonHelper.getBoolean(json, "replace", false),
                        Resources.GSON.getAdapter(TYPE).fromJsonTree(json.get("items")).entrySet().stream().collect(Collectors.toMap(
                                a -> Key.of(a.getKey()),
                                a -> SpellTraits.fromString(a.getValue()).orElse(SpellTraits.EMPTY)
                        ))
                );
            }

            return new TraitSet(
                    JsonHelper.getBoolean(json, "replace", false),
                    SpellTraits.fromString(JsonHelper.getString(json, "traits")).orElse(SpellTraits.EMPTY),
                    StreamSupport.stream(JsonHelper.getArray(json, "items").spliterator(), false)
                        .map(JsonElement::getAsString)
                        .map(Key::of)
                        .collect(Collectors.toSet())
            );
        }

        record TraitMap (
                boolean replace,
                Map<Key, SpellTraits> items) implements TraitStream {
            @Override
            public Stream<Entry<Key, SpellTraits>> entries() {
                return items.entrySet().stream();
            }
        }

        record TraitSet (
                boolean replace,
                SpellTraits traits,
                Set<Key> items) implements TraitStream {
            @Override
            public Stream<Entry<Key, SpellTraits>> entries() {
                return items().stream().map(item -> Map.entry(item, traits()));
            }
        }

        interface Key extends Predicate<ItemConvertible> {
            static Key of(String s) {
                return s.startsWith("#") ? new Tag(TagKey.of(RegistryKeys.ITEM, Identifier.tryParse(s.substring(1)))) : new Id(Identifier.tryParse(s));
            }
            record Tag(TagKey<Item> tag) implements Key {

                @SuppressWarnings("deprecation")
                @Override
                public boolean test(ItemConvertible item) {
                    return item.asItem().getRegistryEntry().isIn(tag);
                }
            }

            record Id(Identifier id) implements Key {
                @Override
                public boolean test(ItemConvertible item) {
                    return Objects.equals(id, Registries.ITEM.getId(item.asItem()));
                }
            }
        }
    }
}
