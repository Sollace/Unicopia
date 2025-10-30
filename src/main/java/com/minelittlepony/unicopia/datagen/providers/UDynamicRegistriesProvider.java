package com.minelittlepony.unicopia.datagen.providers;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class UDynamicRegistriesProvider extends FabricDynamicRegistryProvider {
    private final Set<String> namespaces = Set.of(Unicopia.DEFAULT_NAMESPACE, Unicopia.VANILLA_EXTENSIONS_NAMESPACE);

    public UDynamicRegistriesProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public String getName() {
        return "Unicopia Registries";
    }

    @Override
    protected void configure(WrapperLookup registries, Entries entries) {
        RegistryLoader.DYNAMIC_REGISTRIES.forEach(registry -> {
            addAll(entries, entries.getLookups().getOrThrow(registry.key()));
        });
    }

    private <T> void addAll(Entries entries, RegistryWrapper.Impl<T> wrapper) {
        wrapper.streamKeys()
            .filter(registryKey -> namespaces.contains(registryKey.getValue().getNamespace()))
            .forEach(key -> entries.add(wrapper, key));
    }
}
