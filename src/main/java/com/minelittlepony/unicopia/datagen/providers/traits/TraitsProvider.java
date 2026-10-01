package com.minelittlepony.unicopia.datagen.providers.traits;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.Debug;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.datagen.Datagen;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.DataOutput.OutputType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;

public class TraitsProvider implements DataProvider {
    protected final FabricDataOutput output;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryFuture;

    private final DataCollector<TraitLoader.TraitStream> collector;

    public TraitsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        this.output = output;
        this.registryFuture = registryLookup;
        this.collector = new DataCollector<>(output.getResolver(OutputType.DATA_PACK, "traits"), TraitLoader.TraitStream.CODEC);
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return registryFuture.thenCompose(registries -> {
            var items = registries.getOrThrow(RegistryKeys.ITEM);
            generate(registries, collector.prime());
            Debug.testTraitCoverage(items, Datagen.LOGGER, entry -> collector.streamValues()
                    .flatMap(traitStream -> traitStream.entries())
                    .map(Map.Entry::getKey)
                    .anyMatch(key -> key.test(entry)));
            return collector.upload(writer);
        });
    }

    protected void generate(WrapperLookup registries, BiConsumer<Identifier, TraitLoader.TraitStream> exporter) {
        new UTraitsGenerator().generate(registries, exporter);
        new VanillaTraitsGenerator().generate(registries, exporter);
        new FarmersDelightTraitsGenerator().generate(registries, exporter);
    }

    @Override
    public String getName() {
        return "Item Trait Groups";
    }

}
