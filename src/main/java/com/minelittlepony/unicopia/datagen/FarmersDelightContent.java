package com.minelittlepony.unicopia.datagen;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Function;

import com.mojang.serialization.Lifecycle;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface FarmersDelightContent {
    TagKey<Item> CABBAGE_ROLE_INGREDIENTS = TagKey.of(RegistryKeys.ITEM, id("cabbage_roll_ingredients"));
    TagKey<Item> COMFORT_FOODS = TagKey.of(RegistryKeys.ITEM, id("comfort_foods"));

    Item APPLE_PIE = lateRegister(id("apple_pie"), Registries.ITEM, key -> new Item.Settings().registryKey(key), Item::new);

    private static <T, S> T lateRegister(Identifier id, Registry<T> registry, Function<RegistryKey<T>, S> settings, Function<S, T> value) {
        try {
            Field frozen = SimpleRegistry.class.getDeclaredField("frozen");
            Field intrusiveValueToEntry = SimpleRegistry.class.getDeclaredField("intrusiveValueToEntry");
            Field tagLookup = SimpleRegistry.class.getDeclaredField("tagLookup");
            frozen.setAccessible(true);
            intrusiveValueToEntry.setAccessible(true);
            boolean isFrozen = frozen.getBoolean(registry);
            if (isFrozen) {
                frozen.set(registry, false);
                tagLookup.setAccessible(true);
                tagLookup.set(registry, tagLookup.get(new SimpleRegistry<>(registry.getKey(), Lifecycle.stable())));
            }
            var intrusiveMap = intrusiveValueToEntry.get(registry);
            if (intrusiveMap == null) {
                intrusiveMap = new HashMap<>();
                intrusiveValueToEntry.set(registry, intrusiveMap);
            }
            RegistryKey<T> key = RegistryKey.of(registry.getKey(), id);
            T t = Registry.register(registry, key, value.apply(settings.apply(key)));
            if (isFrozen) {
                registry.freeze();
            }
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void bootstrap() { }

    static Identifier id(String name) {
        return Identifier.of("farmersdelight", name);
    }
}
