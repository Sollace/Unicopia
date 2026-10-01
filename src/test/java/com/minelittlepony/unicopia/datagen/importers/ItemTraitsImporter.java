package com.minelittlepony.unicopia.datagen.importers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.minelittlepony.unicopia.datagen.FarmersDelightContent;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class ItemTraitsImporter implements DataImportProvider.DataImporter {
    static final String INDENT = "    ";

    private final Function<Class<?>, Set<String>> fieldLookup = Util.memoize(owner -> {
        return Arrays.stream(owner.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet());
    });

    @Override
    public void generateCode(RegistryWrapper.WrapperLookup registries, ResourceCollector collector, Consumer<String> output) {
        collector.collect(registries, TraitStream.CODEC, "traits").forEach(entry -> {
            generateCode(entry.getKey(), entry.getValue(), output);
        });
    }

    @Override
    public String getName() {
        return "item_traits.java";
    }

    private void generateCode(Identifier key, TraitLoader.TraitStream entry, Consumer<String> lineConsumer) {
        if (entry instanceof TraitLoader.TraitStream.TraitMap map) {
            lineConsumer.accept("exporter.accept(" + createIdentifierReference(key) + ", TraitMap.builder()");
            map.entries().forEach(i -> {
                if (i.getKey() instanceof TraitLoader.TraitStream.Key.Id id) {
                    lineConsumer.accept(INDENT + INDENT + ".item(" + createItemReference(id) + ", " + createTraitsBuilder(i.getValue()) + ")");
                } else if (i.getKey() instanceof TraitLoader.TraitStream.Key.Tag tag) {
                    lineConsumer.accept(INDENT + INDENT + ".tag(" + createTagReference(tag) + ", " + createTraitsBuilder(i.getValue()) + ")");
                }
            });
            lineConsumer.accept(INDENT + INDENT + ".build());");
        } else if (entry instanceof TraitLoader.TraitStream.TraitSet set) {
            lineConsumer.accept("exporter.accept(" + createIdentifierReference(key) + ", TraitSet.builder(" + createTraitsBuilder(set.traits()) + ")");
            set.items().forEach(i -> {
                if (i instanceof TraitLoader.TraitStream.Key.Id id) {
                    lineConsumer.accept(INDENT + INDENT + ".item(" + createItemReference(id) + ")");
                } else if (i instanceof TraitLoader.TraitStream.Key.Tag tag) {
                    lineConsumer.accept(INDENT + INDENT + ".tag(" + createTagReference(tag) + ")");
                }
            });
            lineConsumer.accept(INDENT + INDENT + ".build());");
        }
    }

    private String createIdentifierReference(Identifier id) {
        if (id.getNamespace().equals(Unicopia.DEFAULT_NAMESPACE)) {
            return String.format("Unicopia.id(\"%s\")", id.getPath());
        }
        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return String.format("Identifier.ofVanilla(\"%s\")", id.getPath());
        }
        if (id.getNamespace().equals(FarmersDelightContent.DEFAULT_NAMESPACE)) {
            return String.format("FarmersDelightContent.id(\"%s\")", id.getPath());
        }
        return String.format("Identifier.of(\"%s\", \"%s\")", id.getNamespace(), id.getPath());
    }

    private String createItemReference(TraitLoader.TraitStream.Key.Id key) {
        var id = key.key().getValue();
        String fieldName = id.getPath().toUpperCase(Locale.ROOT);
        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            if (referenceExists(Items.class, fieldName)) {
                return "Items." + fieldName;
            }
            return "Blocks." + fieldName;
        }
        if (id.getNamespace().equals(Unicopia.DEFAULT_NAMESPACE)) {
            if (referenceExists(UItems.class, fieldName)) {
                return "UItems." + fieldName;
            }
            return "UBlocks." + fieldName;
        }
        if (id.getNamespace().equals(Unicopia.DEFAULT_NAMESPACE)) {
            if (referenceExists(FarmersDelightContent.class, fieldName)) {
                return "FarmersDelightContent." + fieldName;
            } else {
                Datagen.LOGGER.warn("No content defined for farmer'd delight entry: ItemConvertible " + fieldName);
            }
        }

        return createIdentifierReference(id);
    }

    private String createTagReference(TraitLoader.TraitStream.Key.Tag tag) {
        var id = tag.tag().id();
        String fieldName = id.getPath().toUpperCase(Locale.ROOT);
        if (id.getNamespace().contentEquals(Identifier.DEFAULT_NAMESPACE)) {
            return "ItemTags." + fieldName;
        }
        if (id.getNamespace().contentEquals(TagUtil.C_TAG_NAMESPACE)) {
            if (referenceExists(ConventionalItemTags.class, fieldName)) {
                return "ConventionalItemTags." + fieldName;
            }
            return "UConventionalTags.Items." + fieldName;
        }
        if (id.getNamespace().contentEquals(Unicopia.DEFAULT_NAMESPACE)) {
            return "UTags.Items." + fieldName;
        }
        if (id.getNamespace().equals(Unicopia.DEFAULT_NAMESPACE)) {
            if (referenceExists(FarmersDelightContent.class, fieldName)) {
                return "FarmersDelightContent." + fieldName;
            } else {
                Datagen.LOGGER.warn("No content defined for farmer'd delight entry: TagKey<Item> " + fieldName);
            }
        }

        return String.format("TagKey.of(RegistryKeys.ITEM, %s)", createIdentifierReference(id));
    }

    private boolean referenceExists(Class<?> owner, String fieldName) {
        return fieldLookup.apply(owner).contains(fieldName);
    }

    private String createTraitsBuilder(SpellTraits traits) {
        if (traits.isEmpty()) {
            return "SpellTraits.empty()";
        }
        if (traits.size() == 1) {
            var first = traits.stream().findFirst().get();
            return new StringBuilder("SpellTraits.of(Trait.").append(first.getKey().name()).append(", ").append(createFloat(first.getValue())).append(")").toString();
        }
        StringBuilder builder = new StringBuilder("SpellTraits.builder()");
        traits.forEach(entry -> {
            builder.append(".with(Trait.").append(entry.getKey().name()).append(", ").append(createFloat(entry.getValue())).append(")");
        });
        return builder.toString();
    }

    private String createFloat(float value) {
        if (MathHelper.approximatelyEquals((int)value, value)) {
            return String.valueOf((int)value);
        }
        return String.valueOf(value) + "F";
    }
}
