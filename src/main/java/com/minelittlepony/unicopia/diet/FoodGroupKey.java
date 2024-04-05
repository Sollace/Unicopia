package com.minelittlepony.unicopia.diet;

import java.util.function.Function;

import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.Unicopia;
import com.mojang.serialization.Codec;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface FoodGroupKey {
    Function<Identifier, FoodGroupKey> LOOKUP = Util.memoize(id -> {
        return new FoodGroupKey() {
            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public boolean contains(ItemStack stack) {
                var group = PonyDiets.getEffect(id);
                return group != null && group.test(stack);
            }

            @Override
            public boolean equals(Object o) {
                return o == this && (o instanceof FoodGroupKey k && k.id().equals(id()));
            }

            @Override
            public int hashCode() {
                return id().hashCode();
            }
        };
    });
    Function<TagKey<Item>, FoodGroupKey> TAG_LOOKUP = Util.memoize(tag -> {
        return new FoodGroupKey() {
            private boolean check;
            @Override
            public Identifier id() {
                return tag.id();
            }

            @Override
            public boolean contains(ItemStack stack) {
                if (Debug.CHECK_GAME_VALUES && !check) {
                    check = true;
                    if (Registries.ITEM.getEntryList(tag).isEmpty()) {
                        Unicopia.LOGGER.info("Tag is empty: " + tag.id());
                    }
                }

                return stack.isIn(tag);
            }

            @Override
            public boolean equals(Object o) {
                return o == this && (o instanceof FoodGroupKey k && k.id().equals(id()));
            }

            @Override
            public int hashCode() {
                return id().hashCode();
            }
        };
    });
    Function<Identifier, FoodGroupKey> TAG_ID_LOOKUP = id -> TAG_LOOKUP.apply(TagKey.of(RegistryKeys.ITEM, id));
    Codec<FoodGroupKey> CODEC = Identifier.CODEC.xmap(LOOKUP, FoodGroupKey::id);
    Codec<FoodGroupKey> TAG_CODEC = TagKey.unprefixedCodec(RegistryKeys.ITEM).xmap(TAG_LOOKUP, k -> TagKey.of(RegistryKeys.ITEM, k.id()));

    Identifier id();

    boolean contains(ItemStack stack);
}
