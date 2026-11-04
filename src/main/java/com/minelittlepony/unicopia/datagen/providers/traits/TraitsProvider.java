package com.minelittlepony.unicopia.datagen.providers.traits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.datagen.Datagen;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.data.DataOutput.OutputType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TraitsProvider implements DataProvider {
    protected final FabricDataOutput output;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryFuture;
    private final CompletableFuture<Debug.ItemTagPool<?>> tagLookupFuture;

    private final DataCollector<TraitLoader.TraitStream> collector;

    public TraitsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup, CompletableFuture<Debug.ItemTagPool<?>> tagProvider) {
        this.output = output;
        this.registryFuture = registryLookup;
        this.tagLookupFuture = tagProvider;
        this.collector = new DataCollector<>(output.getResolver(OutputType.DATA_PACK, "unicopia/traits"), TraitLoader.TraitStream.CODEC);
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return tagLookupFuture.thenCompose(tagLookup -> registryFuture.thenCompose(registries -> {
            var consumer = collector.prime();

            Set<TagKey<Item>> allTags = new HashSet<>();
            Set<RegistryEntry<Item>> allItems = new HashSet<>();

            generate(registries, (id, stream) -> {
                Map<TagKey<Item>, SpellTraits> tagKeys = new HashMap<>();
                Map<RegistryEntry<Item>, SpellTraits> itemKeys = new HashMap<>();
                UnaryOperator<TagKey<Item>> tagChecker = tag -> {
                    tagLookup.get(tag).ifPresentOrElse(entries -> {
                        if (entries.size() == 0) {
                            Datagen.LOGGER.warn("Trait stream {} references empty tag {}", id, tag);
                        }
                    }, () -> Datagen.LOGGER.warn("Trait stream {} references unregistered tag {}", id, tag));
                    return tag;
                };
                stream.entries().forEach(entry -> {
                    switch (entry.getKey()) {
                        case TraitStream.Key.Id idKey -> itemKeys.put(tagLookup.get(idKey.key()), entry.getValue());
                        case TraitStream.Key.Tag tagKey -> tagKeys.put(tagChecker.apply(tagKey.tag()), entry.getValue());
                    }
                });
                boolean compareTraits = stream instanceof TraitStream.TraitMap;
                String result = itemKeys.entrySet().stream().map(item -> {
                    return Map.entry(item.getKey(), tagKeys.entrySet().stream().filter(tag -> {
                        return item.getKey().isIn(tag.getKey()) && (!compareTraits || item.getValue().equals(tag.getValue()));
                    }).map(tag -> tag.getKey().id().toString()).collect(Collectors.joining(",")));
                }).filter(i -> !i.getValue().isEmpty()).map(entry -> entry.getKey().getIdAsString() + "[" + entry.getValue() + "]").collect(Collectors.joining("," + System.lineSeparator() + "  "));
                if (!result.isEmpty()) {
                    Datagen.LOGGER.error("Item explicitly added to trait stream {} is already matched by one or more tags" + (compareTraits ? " with the same traits" :"") + ":{}{}", id, System.lineSeparator(), result);
                }
                allTags.addAll(tagKeys.keySet());
                allItems.addAll(itemKeys.keySet());
                consumer.accept(id, stream);
            });
            Debug.testTraitCoverage(tagLookup, Datagen.LOGGER, entry -> allItems.contains(entry) || allTags.stream().anyMatch(entry::isIn));
            return collector.upload(writer);
        }));
    }

    protected void generate(WrapperLookup registries, BiConsumer<Identifier, TraitLoader.TraitStream> exporter) {
        new UTraitsGenerator().generate(registries, exporter);
        new VanillaTraitsGenerator().generate(registries, exporter);
        new FarmersDelightTraitsGenerator().generate(registries, exporter);
    }

    @Override
    public String getName() {
        return "Item Trait Groups";
    }

}
