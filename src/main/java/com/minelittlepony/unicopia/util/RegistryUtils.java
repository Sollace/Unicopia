package com.minelittlepony.unicopia.util;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.serialization.Lifecycle;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryList.Named;
import net.minecraft.world.World;

public interface RegistryUtils {
    static <T> Registry<T> createSimple(Identifier id) {
        return FabricRegistryBuilder.from(new SimpleRegistry<T>(RegistryKey.ofRegistry(id), Lifecycle.stable())).buildAndRegister();
    }

    static <T> Registry<T> createDefaulted(Identifier id, String def) {
        return FabricRegistryBuilder.from(new SimpleDefaultedRegistry<T>(def, RegistryKey.ofRegistry(id), Lifecycle.stable(), false)).buildAndRegister();
    }

    static <T> RegistryEntryList<T> entriesForTag(World world, TagKey<T> key) {
        return world.getRegistryManager().get(key.registry()).getOrCreateEntryList(key);
    }

    static <T> Stream<T> valuesForTag(World world, TagKey<T> key) {
        return entriesForTag(world, key).stream().map(RegistryEntry::value);
    }

    static <T> Optional<T> pickRandom(World world, TagKey<T> key) {
        return world.getRegistryManager().getOptional(key.registry())
            .flatMap(registry -> registry.getEntryList(key))
            .flatMap(entries -> entries.getRandom(world.random))
            .map(RegistryEntry::value);
    }

    static <T> Optional<T> pickRandom(World world, TagKey<T> key, Predicate<T> filter) {
        return Util.getRandomOrEmpty(world.getRegistryManager().getOptional(key.registry())
            .flatMap(registry -> registry.getEntryList(key))
            .stream()
            .flatMap(Named::stream)
            .map(RegistryEntry::value)
            .filter(filter)
            .toList(), world.random);
    }

    static <T> Optional<RegistryEntry<T>> pickRandomEntry(World world, TagKey<T> key, Predicate<RegistryEntry<T>> filter) {
        return Util.getRandomOrEmpty(world.getRegistryManager().getOptional(key.registry())
            .flatMap(registry -> registry.getEntryList(key))
            .stream()
            .flatMap(Named::stream)
            .filter(filter)
            .toList(), world.random);
    }

    static <T> boolean isIn(World world, T obj, RegistryKey<? extends Registry<T>> registry, TagKey<T> tag) {
        return world.getRegistryManager().get(registry).getEntry(obj).isIn(tag);
    }

    static <T> Identifier getId(World world, T obj, RegistryKey<? extends Registry<T>> registry) {
        return world.getRegistryManager().get(registry).getId(obj);
    }
}
