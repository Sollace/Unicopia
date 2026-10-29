package com.minelittlepony.unicopia.datagen;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Untyped;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.impl.datagen.ForcedTagEntry;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.data.tag.TagProvider.TagLookup;
import net.minecraft.item.Item;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagEntry.ValueGetter;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class DatagenItemTagsRegistry {
    private final CompletableFuture<?> resolutionFuture;

    private final Map<Identifier, TagBuilder> builders = new HashMap<>();

    public DatagenItemTagsRegistry(FabricDataOutput output, CompletableFuture<TagLookup<Item>> downstreamFuture) {
        this.resolutionFuture = downstreamFuture;
    }

    public TagBuilder register(TagKey<Item> tag, TagBuilder builder) {
        builders.put(tag.id(), builder);
        return builder;
    }


    private List<TagEntry> build(TagBuilder builder) {
        return builder.build().stream().map(entry -> {
            if (entry instanceof ForcedTagEntry forcedTag) {
                var output = new AtomicReference<>(entry);
                forcedTag.resolve(new ValueGetter<TagEntry>() {
                    @Override
                    public TagEntry direct(Identifier id, boolean required) {
                        return required ? TagEntry.createTag(id) : TagEntry.createOptionalTag(id);
                    }

                    @Nullable
                    @Override
                    public Collection<TagEntry> tag(Identifier id) {
                        return null;
                    }
                }, output::set);
                return output.get();
            }
            return entry;
        }).toList();
    }

    public CompletableFuture<Debug.ItemTagPool<?>> getTagsFuture() {
        return resolutionFuture.thenApplyAsync(o -> {
            MutableRegistry<Item> registry = Untyped.cast(Registries.ITEM);

            var registryKey = registry.getKey();
            var packManager = ModResourcePackUtil.createClientManager();
            packManager.scanPacks();
            packManager.getIds().forEach(profile -> {
                if (!Unicopia.DEFAULT_NAMESPACE.contentEquals(profile)) {
                    packManager.enable(profile);
                    Datagen.LOGGER.info("Enabled data pack {}", profile);
                }
            });

            var packs = packManager.createResourcePacks();
            Datagen.LOGGER.info("Initialised {} data packs", packs.size());

            TagGroupLoader<RegistryEntry<Item>> tagGroupLoader = new TagGroupLoader<>(TagGroupLoader.EntrySupplier.forReload(registry), RegistryKeys.getTagPath(registryKey));
            var loadedTags = new HashMap<>(tagGroupLoader.loadTags(new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, packs)));
            Datagen.LOGGER.info("Prepared to load {} tags from [{}]", loadedTags.size(), loadedTags.keySet().stream().map(Identifier::getNamespace).distinct().collect(Collectors.joining(",")));

            var allTagIds = new HashSet<>(loadedTags.keySet());
            allTagIds.addAll(builders.keySet());
            builders.forEach((id, builder) -> {
                loadedTags.compute(id, (i, existing) -> {
                    var injectedEntries = build(builder).stream().map(entry -> {
                        entry.forEachRequiredTagId(requiredTag -> {
                            if (!allTagIds.contains(requiredTag)) {
                                Datagen.LOGGER.error("Tag {} is missing reference to tag {}", id, requiredTag);
                            }
                        });
                        entry.resolve(new ValueGetter<RegistryKey<Item>>() {
                            @Override
                            public RegistryKey<Item> direct(Identifier id, boolean required) {
                                return required ? RegistryKey.of(RegistryKeys.ITEM, id) : null;
                            }

                            @Nullable
                            @Override
                            public Collection<RegistryKey<Item>> tag(Identifier id) {
                                return null;
                            }
                        }, itemKey -> {
                            if (registry.getOptional(itemKey).isEmpty()) {
                                Datagen.LOGGER.error("Tag {} is missing reference to item {}", id, itemKey);
                            }
                        });
                        return new TagGroupLoader.TrackedEntry(entry, "datagen");
                    });
                    if (existing != null) {
                        injectedEntries = Stream.concat(existing.stream(), injectedEntries);
                    }
                    return injectedEntries.collect(Collectors.toList());
                });
            });
            Datagen.LOGGER.info("Added {} tags from builders in namespaces [{}]", builders.size(),
                    builders.keySet().stream().map(Identifier::getNamespace).distinct().collect(Collectors.joining(","))
            );

            Map<TagKey<Item>, List<RegistryEntry<Item>>> builtTags = tagGroupLoader.buildGroup(loadedTags)
                    .entrySet()
                    .stream()
                    .collect(Collectors.toUnmodifiableMap(
                            entry -> TagKey.of(registryKey, entry.getKey()),
                            Entry::getValue
                    ));

            registry.startTagReload(new TagGroupLoader.RegistryTags<>(registryKey, builtTags)).apply();

            return Debug.ItemTagPool.ofRegistry(registry);
        }, Util.getMainWorkerExecutor());
    }
}










