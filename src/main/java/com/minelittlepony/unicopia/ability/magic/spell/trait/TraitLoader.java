package com.minelittlepony.unicopia.ability.magic.spell.trait;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.include.com.google.common.base.Preconditions;

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
import net.minecraft.registry.entry.RegistryEntry;
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

        SpellTraits.load(Registries.ITEM.streamEntries()
                .map(reference -> Map.entry(
                        reference.registryKey(),
                        newRegistry.stream()
                            .filter(p -> p.getKey().test(reference))
                            .map(Map.Entry::getValue)
                            .reduce(SpellTraits::union)
                            .orElse(SpellTraits.EMPTY)
                ))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        profiler.endTick();
    }

    public interface TraitStream {
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

            public static Builder builder() {
                return new Builder();
            }

            public static class Builder {
                private final Map<Key, SpellTraits> items = new HashMap<>();
                private Builder() {}

                public Builder tag(TagKey<Item> tag, SpellTraits.Builder traits) {
                    return tag(tag, traits.build());
                }

                public Builder tag(TagKey<Item> tag, SpellTraits traits) {
                    items.put(new Key.Tag(tag), traits);
                    return this;
                }

                public Builder item(ItemConvertible item, SpellTraits.Builder traits) {
                    return item(item, traits.build());
                }

                @SuppressWarnings("deprecation")
                public Builder item(ItemConvertible item, SpellTraits traits) {
                    items.put(new Key.Id(item.asItem().getRegistryEntry().registryKey()), traits);
                    return this;
                }

                public TraitMap build() {
                    return new TraitMap(false, Map.copyOf(items));
                }
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

            public static Builder builder(SpellTraits.Builder traits) {
                return new Builder(traits.build());
            }

            public static Builder builder(SpellTraits traits) {
                return new Builder(traits);
            }

            public static final class Builder {
                private final SpellTraits traits;
                private final Set<Key> keys = new HashSet<>();

                private final Set<TagKey<Item>> tags = new HashSet<>();
                private final Set<RegistryEntry<Item>> items = new HashSet<>();

                private Builder(SpellTraits traits) {
                    this.traits = traits;
                }

                public Builder tag(TagKey<Item> tag) {
                    keys.add(new Key.Tag(tag));
                    tags.add(tag);
                    return this;
                }

                @SuppressWarnings("deprecation")
                public Builder item(ItemConvertible item) {
                    if (!keys.add(new Key.Id(item.asItem().getRegistryEntry().registryKey()))) {
                        throw new IllegalArgumentException("Item specified multiple times: " + item);
                    }
                    items.add(item.asItem().getRegistryEntry());
                    return this;
                }

                public Builder apply(UnaryOperator<Builder> action) {
                    return action.apply(this);
                }

                public TraitSet build() {
                    String result = items.stream().map(item -> {
                        return Map.entry(item, tags.stream().filter(tag -> item.isIn(tag)).map(tag -> tag.id().toString()).collect(Collectors.joining(",")));
                    }).filter(i -> !i.getValue().isEmpty()).map(entry -> entry.getKey() + "[" + entry.getValue() + "]").collect(Collectors.joining(","));
                    Preconditions.checkState(result.isEmpty(), "Item explicitly added will be matched by tags: " + result);
                    return new TraitSet(false, traits, Set.copyOf(keys));
                }
            }
        }

        interface Key extends Predicate<RegistryEntry<Item>> {
            Codec<Key> CODEC = Codec.xor(Tag.CODEC, Id.CODEC).xmap(
                    Either::unwrap,
                    key -> key instanceof Tag l ? Either.left(l) : Either.right((Id)key)
            );

            record Tag(TagKey<Item> tag) implements Key {
                static final Codec<Tag> CODEC = TagKey.codec(RegistryKeys.ITEM).xmap(Tag::new, Tag::tag);

                @Override
                public boolean test(RegistryEntry<Item> item) {
                    return item.isIn(tag);
                }
            }

            record Id(RegistryKey<Item> key) implements Key {
                static final Codec<Id> CODEC = RegistryKey.createCodec(RegistryKeys.ITEM).xmap(Id::new, Id::key);

                @Override
                public boolean test(RegistryEntry<Item> item) {
                    return item.matchesKey(key);
                }
            }
        }
    }
}
