package com.minelittlepony.unicopia.datagen.providers.traits;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream.Key;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public abstract class TraitsGenerator {
    public abstract void generate(WrapperLookup registries, BiConsumer<Identifier, TraitLoader.TraitStream> exporter);

    protected TraitLoader.TraitStream.TraitSet.Builder applyItemsWhere(TraitLoader.TraitStream.TraitSet.Builder builder, RegistryWrapper<Item> items, Predicate<String> idPredicate) {
        items.streamEntries().filter(i -> {
            String path = i.registryKey().getValue().getPath();
            return idPredicate.test(path);
        }).forEach(i -> builder.item(i.value()));
        return builder;
    }

    protected static Key tag(TagKey<Item> tag) {
        return new Key.Tag(tag);
    }

    @SuppressWarnings("deprecation")
    protected static Key item(ItemConvertible item) {
        return new Key.Id(item.asItem().getRegistryEntry().registryKey());
    }

    protected static SpellTraits.Builder boatTraits() {
        return SpellTraits.builder().with(Trait.KNOWLEDGE, 3).with(Trait.ORDER, 1);
    }
}
