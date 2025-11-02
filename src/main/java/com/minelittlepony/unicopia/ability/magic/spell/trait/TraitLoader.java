package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
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
                        JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();

                        TraitStream set = TraitStream.CODEC.decode(JsonOps.INSTANCE, data).getOrThrow(error -> new JsonParseException(error)).getFirst();

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
        Codec<TraitStream> CODEC = Codec.xor(TraitMap.CODEC, TraitSet.CODEC).xmap(
            Either::unwrap,
            stream -> stream instanceof TraitMap l ? Either.left(l) : Either.right((TraitSet)stream)
        );

        boolean replace();

        Stream<Map.Entry<Key, SpellTraits>> entries();

        record TraitMap (
                boolean replace,
                Map<Key, SpellTraits> items) implements TraitStream {
            static final Codec<TraitMap> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.BOOL.optionalFieldOf("replace", false).forGetter(TraitMap::replace),
                    Codec.unboundedMap(Key.CODEC, SpellTraits.createStringCodec(" ")).fieldOf("items").forGetter(TraitMap::items)
            ).apply(i, TraitMap::new));

            @Override
            public Stream<Entry<Key, SpellTraits>> entries() {
                return items.entrySet().stream();
            }
        }

        record TraitSet (
                boolean replace,
                SpellTraits traits,
                Set<Key> items) implements TraitStream {
            static final Codec<TraitSet> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.BOOL.optionalFieldOf("replace", false).forGetter(TraitSet::replace),
                    SpellTraits.createStringCodec(" ").fieldOf("traits").forGetter(TraitSet::traits),
                    CodecUtils.setOf(Key.CODEC).fieldOf("items").forGetter(TraitSet::items)
            ).apply(i, TraitSet::new));

            @Override
            public Stream<Entry<Key, SpellTraits>> entries() {
                return items().stream().map(item -> Map.entry(item, traits()));
            }
        }

        interface Key extends Predicate<ItemConvertible> {
            Codec<Key> CODEC = Codec.xor(Tag.CODEC, Id.CODEC).xmap(
                    Either::unwrap,
                    key -> key instanceof Tag l ? Either.left(l) : Either.right((Id)key)
            );

            record Tag(TagKey<Item> tag) implements Key {
                static final Codec<Tag> CODEC = TagKey.codec(RegistryKeys.ITEM).xmap(Tag::new, Tag::tag);

                @SuppressWarnings("deprecation")
                @Override
                public boolean test(ItemConvertible item) {
                    return item.asItem().getRegistryEntry().isIn(tag);
                }
            }

            record Id(RegistryKey<Item> key) implements Key {
                static final Codec<Id> CODEC = RegistryKey.createCodec(RegistryKeys.ITEM).xmap(Id::new, Id::key);

                @SuppressWarnings("deprecation")
                @Override
                public boolean test(ItemConvertible item) {
                    return item.asItem().getRegistryEntry().matchesKey(key);
                }
            }
        }
    }
}
