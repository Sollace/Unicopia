package com.minelittlepony.unicopia.datagen.importers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.serialization.Lifecycle;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.Impl;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.registry.entry.RegistryEntryList.Named;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Util;

public class DataImportProvider implements DataProvider {
    private final ResourceCollector collector;
    private final Path output;

    private final CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture;

    public DataImportProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        this.registriesFuture = registriesFuture;
        collector = new ResourceCollector(output.getPath().getParent().resolve("resources"), ResourceType.SERVER_DATA);
        this.output = output.getPath().getParent().resolve("generated-code");
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        try {
            Files.createDirectories(this.output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return registriesFuture.thenAccept(registries -> {
            registries = new DummyRegistryWrapperLookup(registries);
            runDataImport(registries, new ItemTraitsImporter());
            runDataImport(registries, new SpellbookPageImporter());
        });
    }

    private void runDataImport(RegistryWrapper.WrapperLookup registries, DataImporter importer) {
        Path file = output.resolve(importer.getName());
        Stopwatch timer = Stopwatch.createUnstarted();
        Datagen.LOGGER.info("Starting data exporter: {}", importer.getName());
        timer.start();
        try (var writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            importer.generateCode(registries, collector, line -> {
                try {
                    writer.append(line);
                    writer.append(System.lineSeparator());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Datagen.LOGGER.info("{} finished after {} ms", importer.getName(), timer.elapsed(TimeUnit.MILLISECONDS));
    }

    @Override
    public String getName() {
        return "Existing Data Migration";
    }

    public interface DataImporter {
        void generateCode(RegistryWrapper.WrapperLookup registries, ResourceCollector collector, Consumer<String> lineConsumer);

        String getName();
    }

    record DummyRegistryWrapperLookup(RegistryWrapper.WrapperLookup delegate, Function<RegistryKey<? extends Registry<?>>, Impl<?>> registryLookup) implements RegistryWrapper.WrapperLookup {
        public DummyRegistryWrapperLookup(RegistryWrapper.WrapperLookup delegate) {
            this(delegate, Util.memoize(key -> {
                return new Impl<>() {
                    @Override
                    public Stream<Reference<Object>> streamEntries() {
                        return Stream.empty();
                    }

                    @Override
                    public Stream<Named<Object>> getTags() {
                        return Stream.empty();
                    }

                    @Override
                    public Optional<Reference<Object>> getOptional(RegistryKey<Object> key) {
                        return Optional.of(Reference.standAlone(this, key));
                    }

                    @Override
                    public Optional<Named<Object>> getOptional(TagKey<Object> tag) {
                        return Optional.empty();
                    }

                    @Override
                    public RegistryKey<? extends Registry<? extends Object>> getKey() {
                        return key;
                    }

                    @Override
                    public Lifecycle getLifecycle() {
                        return Lifecycle.stable();
                    }
                };
            }));
        }

        @Override
        public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
            return Stream.concat(delegate.streamAllRegistryKeys(), Stream.of(RegistryKeys.RECIPE));
        }

        @Override
        public <T> Optional<? extends Impl<T>> getOptional(RegistryKey<? extends Registry<? extends T>> registryRef) {
            return delegate.getOptional(registryRef).or(() -> {
                return Optional.of(Untyped.cast(registryLookup.apply(registryRef)));
            });
        }

    }
}










