package com.minelittlepony.unicopia.datagen;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface FarmersDelightContent {
    TagKey<Item> CABBAGE_ROLE_INGREDIENTS = TagKey.of(RegistryKeys.ITEM, id("cabbage_roll_ingredients"));
    TagKey<Item> COMFORT_FOODS = TagKey.of(RegistryKeys.ITEM, id("comfort_foods"));

    Item APPLE_PIE = lateRegister(id("apple_pie"), Registries.ITEM, () -> new Item(new Item.Settings()));

    private static <T> T lateRegister(Identifier id, Registry<T> registry, Supplier<T> value) {
        try {
            Field frozen = SimpleRegistry.class.getDeclaredField("frozen");
            Field intrusiveValueToEntry = SimpleRegistry.class.getDeclaredField("intrusiveValueToEntry");
            frozen.setAccessible(true);
            intrusiveValueToEntry.setAccessible(true);
            boolean isFrozen = frozen.getBoolean(registry);
            if (isFrozen) {
                frozen.set(registry, false);
            }
            var intrusiveMap = intrusiveValueToEntry.get(registry);
            if (intrusiveMap == null) {
                intrusiveValueToEntry.set(registry, new HashMap<>());
            }
            T t = Registry.register(registry, id, value.get());
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
