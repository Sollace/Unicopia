package com.minelittlepony.unicopia.datagen.providers;

import net.minecraft.block.Block;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.MultipartBlockModelDefinitionCreator;
import net.minecraft.client.render.model.json.WeightedVariant;

final class FireModels {
    static void registerSoulFire(BlockStateModelGenerator modelGenerator, Block fire, Block texture) {
        WeightedVariant weightedVariant = modelGenerator.getFireFloorModels(texture);
        WeightedVariant weightedVariant2 = modelGenerator.getFireSideModels(texture);
        modelGenerator.blockStateCollector
            .accept(
                MultipartBlockModelDefinitionCreator.create(fire)
                    .with(weightedVariant)
                    .with(weightedVariant2)
                    .with(weightedVariant2.apply(BlockStateModelGenerator.ROTATE_Y_90))
                    .with(weightedVariant2.apply(BlockStateModelGenerator.ROTATE_Y_180))
                    .with(weightedVariant2.apply(BlockStateModelGenerator.ROTATE_Y_270))
            );
    }

}
