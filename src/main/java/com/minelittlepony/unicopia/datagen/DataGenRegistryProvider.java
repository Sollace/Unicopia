package com.minelittlepony.unicopia.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.Lifecycle;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryBuilder.BootstrapFunction;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.registry.tag.TagKey;

public abstract class DataGenRegistryProvider<T> implements BootstrapFunction<T> {

    private final RegistryKey<Registry<T>> registryRef;
    private final List<RegistryKey<T>> keys = new ArrayList<>();

    public DataGenRegistryProvider(RegistryKey<Registry<T>> registryRef) {
        this.registryRef = registryRef;
    }

    public final void addToPack(Pack pack) {
        pack.addProvider(this::createDataGenerator);
        pack.addProvider(this::createTagProvider);
    }

    protected List<RegistryKey<T>> getKeys() {
        return keys;
    }

    protected abstract void configureTags(TagProvider tagProvider, WrapperLookup lookup);

    public FabricDynamicRegistryProvider createDataGenerator(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        return new FabricDynamicRegistryProvider(output, registriesFuture) {
            @Override
            public String getName() {
                return "Unicopia Registry Data Gen " + registryRef.getValue();
            }

            @Override
            protected void configure(WrapperLookup registries, Entries entries) {
                DataGenRegistryProvider.this.run(new Registerable<T>() {
                    @Override
                    public Reference<T> register(RegistryKey<T> key, T value, Lifecycle lifecycle) {
                        keys.add(key);
                        entries.add(key, value);
                        return null;
                    }

                    @Override
                    public <S> RegistryEntryLookup<S> getRegistryLookup(RegistryKey<? extends Registry<? extends S>> registryRef) {
                        return registries.getWrapperOrThrow(registryRef);
                    }
                });
            }
        };
    }

    public FabricTagProvider<T> createTagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        return new TagProvider(output, registriesFuture);
    }

    protected class TagProvider extends FabricTagProvider<T> {
        public TagProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
            super(output, DataGenRegistryProvider.this.registryRef, registriesFuture);
        }

        @Override
        public FabricTagBuilder getOrCreateTagBuilder(TagKey<T> tag) {
            return super.getOrCreateTagBuilder(tag);
        }

        @Override
        protected void configure(WrapperLookup lookup) {
            DataGenRegistryProvider.this.configureTags(this, lookup);
        }
    }
}
