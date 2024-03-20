package com.minelittlepony.unicopia.datagen.providers;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.DataCollector;
import com.minelittlepony.unicopia.server.world.UTreeGen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataOutput.PathResolver;
import net.minecraft.registry.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;

public class SeasonsGrowthRatesProvider implements DataProvider {

    private final PathResolver pathResolver;

    public SeasonsGrowthRatesProvider(FabricDataOutput output) {
        this.pathResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "seasons/crop");
    }

    @Override
    public String getName() {
        return "Seasons Growth Rates";
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        DataCollector collectedData = new DataCollector(pathResolver);
        var exporter = collectedData.prime();
        generate((block, crop) -> {
            exporter.accept(Registries.BLOCK.getId(block), crop::toJson);
        });
        return collectedData.upload(writer);
    }

    private void generate(BiConsumer<Block, Crop> exporter) {
        Crop greenApple = new Crop(0.5F, 0.6F, 1, 0);
        exporter.accept(UBlocks.GREEN_APPLE_LEAVES, greenApple);
        exporter.accept(UBlocks.GREEN_APPLE_SPROUT, greenApple);
        exporter.accept(UTreeGen.GREEN_APPLE_TREE.sapling().get(), greenApple);

        Crop sourApple = new Crop(0.25F, 0.5F, 1, 0.5F);
        exporter.accept(UBlocks.SOUR_APPLE_LEAVES, sourApple);
        exporter.accept(UBlocks.SOUR_APPLE_SPROUT, sourApple);
        exporter.accept(UTreeGen.SOUR_APPLE_TREE.sapling().get(), sourApple);

        Crop sweetApple = new Crop(1, 1, 0.6F, 0);
        exporter.accept(UBlocks.SWEET_APPLE_LEAVES, sweetApple);
        exporter.accept(UBlocks.SWEET_APPLE_SPROUT, sweetApple);
        exporter.accept(UTreeGen.SWEET_APPLE_TREE.sapling().get(), sweetApple);

        Crop goldenOak = new Crop(1.5F, 1.4F, 0.6F, 0);
        exporter.accept(UBlocks.GOLDEN_OAK_LEAVES, goldenOak);
        exporter.accept(UBlocks.GOLDEN_OAK_SPROUT, goldenOak);
        exporter.accept(UTreeGen.GOLDEN_APPLE_TREE.sapling().get(), goldenOak);

        Crop palm = new Crop(1.1F, 0.9F, 0.2F, 0);
        exporter.accept(UBlocks.PALM_LEAVES, palm);
        exporter.accept(UBlocks.BANANAS, palm);
        exporter.accept(UTreeGen.BANANA_TREE.sapling().get(), palm);

        Crop mango = new Crop(1, 1.6F, 0.5F, 0);
        exporter.accept(UBlocks.MANGO_LEAVES, mango);
        exporter.accept(UTreeGen.MANGO_TREE.sapling().get(), mango);

        Crop oats = new Crop(0.6F, 1, 1, 0);
        exporter.accept(UBlocks.OATS_CROWN, oats);
        exporter.accept(UBlocks.OATS_STEM, oats);
        exporter.accept(UBlocks.OATS, oats);

        exporter.accept(UBlocks.ROCKS, new Crop(1, 1, 1, 1));
        exporter.accept(UBlocks.PINEAPPLE, palm);
    }

    record Crop(float spring, float summer, float fall, float winter) {

        JsonElement toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("spring", spring);
            json.addProperty("summer", summer);
            json.addProperty("winter", winter);
            json.addProperty("fall", fall);
            return json;
        }
    }
}
