package com.minelittlepony.unicopia.util.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class DynamicRegistry<T> implements RegistryBuilder.BootstrapFunction<T> {
    private final RegistryKey<Registry<T>> registry;
    private final Map<RegistryKey<T>, Entry<T>> keys = new HashMap<>();
    private final Registerant<T> valueFactory;

    public DynamicRegistry(RegistryKey<Registry<T>> registry, Registerant<T> valueFactory) {
        this.registry = registry;
        this.valueFactory = valueFactory;
        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.getOptional(registry).ifPresent(r -> {
                AtomicBoolean added = new AtomicBoolean(false);
                registries.registerEntryAdded(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, (raw, id, value) -> {
                    if (added.getAndSet(true)) {
                        return;
                    }
                    final WrapperLookup lookup = registries.asDynamicRegistryManager()::getWrapperOrThrow;
                    keys.forEach((key, entry) -> {
                        if (!r.contains(key)) {
                            Registry.register(r, key, entry.factory().apply(lookup, key));
                        }
                    });
                });
            });
        });
    }

    @Override
    public void run(Registerable<T> registerable) {
        final WrapperLookup lookup = registerable::getRegistryLookup;
        keys.forEach((key, entry) -> registerable.register(key, entry.factory().apply(lookup, key)));
    }

    public RegistryKey<T> register(Identifier id) {
        return register(id, valueFactory);
    }

    public RegistryKey<T> register(Identifier id, Registerant<T> valueFactory) {
        return keys.computeIfAbsent(RegistryKey.of(registry, id), k -> new Entry<>(k, valueFactory)).key();
    }

    record Entry<T>(RegistryKey<T> key, Registerant<T> factory) {}

    public interface Registerant<T> {
        T apply(WrapperLookup lookup, RegistryKey<T> key);
    }

    public interface WrapperLookup {
        <S> RegistryEntryLookup<S> getRegistryLookup(RegistryKey<? extends Registry<? extends S>> registryRef);
    }
}
