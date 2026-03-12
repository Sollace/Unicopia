package com.minelittlepony.unicopia.datagen.providers;

import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.block.state.ReversableBlockStateConverter;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
        var exporter = collector.prime(Entry.CODEC);

        new StateMapGenerator().generate((id, builder) -> {
            exporter.accept(id.getId(), new Entry(builder.build(), false));
        });

        return collector.upload(writer);
    }


    record Entry(ReversableBlockStateConverter entries, boolean replace) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                ReversableBlockStateConverter.codec().fieldOf("entries").forGetter(Entry::entries),
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(Entry::replace)
        ).apply(i, Entry::new));
    }
}
