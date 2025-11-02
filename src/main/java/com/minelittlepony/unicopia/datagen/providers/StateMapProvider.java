package com.minelittlepony.unicopia.datagen.providers;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.datagen.DataCollector;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

public class StateMapProvider implements DataProvider {
    private final DataCollector collector;

    public StateMapProvider(FabricDataOutput output) {
        collector = new DataCollector(output.getResolver(DataOutput.OutputType.DATA_PACK, "state_maps"));
    }

    @Override
    public String getName() {
        return "Block State Maps";
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        var exporter = collector.prime();

        new StateMapGenerator().generate((id, builder) -> {
            exporter.accept(id.getId(), () -> builder.encode().getOrThrow());
        });

        return collector.upload(writer);
    }

}
