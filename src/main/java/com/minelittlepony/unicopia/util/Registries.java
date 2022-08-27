package com.minelittlepony.unicopia.util;

import java.util.stream.Stream;

import com.mojang.serialization.Lifecycle;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.*;
import net.minecraft.world.World;

public interface Registries {
    static <T> Registry<T> createSimple(Identifier id) {
        return FabricRegistryBuilder.from(new SimpleRegistry<T>(RegistryKey.ofRegistry(id), Lifecycle.stable(), null)).buildAndRegister();
    }

    static <T> Registry<T> createDefaulted(Identifier id, String def) {
        return FabricRegistryBuilder.from(new DefaultedRegistry<T>(def, RegistryKey.ofRegistry(id), Lifecycle.stable(), null)).buildAndRegister();
    }

    static <T> RegistryEntryList<T> entriesForTag(World world, TagKey<T> key) {
        return world.getRegistryManager().get(key.registry()).getOrCreateEntryList(key);
    }

    static <T> Stream<T> valuesForTag(World world, TagKey<T> key) {
        return entriesForTag(world, key).stream().map(RegistryEntry::value);
    }
}
