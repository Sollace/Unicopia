package com.minelittlepony.unicopia.util.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class DynamicRegistry<T> implements RegistryBuilder.BootstrapFunction<T> {
    private final RegistryKey<Registry<T>> registry;
    private final Map<RegistryKey<T>, Entry<T>> keys = new HashMap<>();
    private final Function<RegistryKey<T>, T> valueFactory;

    public DynamicRegistry(RegistryKey<Registry<T>> registry, Function<RegistryKey<T>, T> valueFactory) {
        this.registry = registry;
        this.valueFactory = valueFactory;

        DynamicRegistrySetupCallback.EVENT.register(registries -> {
            registries.getOptional(registry).ifPresent(r -> {
                keys.forEach((key, entry)-> Registry.register(r, key.getValue(), entry.factory().apply(key)));
            });
        });
    }

    @Override
    public void run(Registerable<T> registerable) {
        keys.forEach((key, entry) -> registerable.register(key, entry.factory().apply(key)));
    }

    public RegistryKey<T> register(Identifier id) {
        return register(id, valueFactory);
    }

    public RegistryKey<T> register(Identifier id, Function<RegistryKey<T>, T> valueFactory) {
        return keys.computeIfAbsent(RegistryKey.of(registry, id), k -> new Entry<>(k, valueFactory)).key();
    }

    record Entry<T>(RegistryKey<T> key, Function<RegistryKey<T>, T> factory) {}

}
