package com.minelittlepony.unicopia.datagen.providers;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.MultipartBlockStateSupplier;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.util.Identifier;

final class FireModels {
    static void registerSoulFire(BlockStateModelGenerator modelGenerator, Block fire, Block texture) {
        List<Identifier> floorModels = getFireModels(modelGenerator, texture, "_floor").toList();
        List<Identifier> sideModels = Stream.concat(
            getFireModels(modelGenerator, texture, "_side"),
            getFireModels(modelGenerator, texture, "_side_alt")
        ).toList();
        modelGenerator.blockStateCollector.accept(MultipartBlockStateSupplier.create(fire)
                .with(BlockStateModelGenerator.buildBlockStateVariants(floorModels, UnaryOperator.identity()))
                .with(BlockStateModelGenerator.buildBlockStateVariants(sideModels, UnaryOperator.identity()))
                .with(BlockStateModelGenerator.buildBlockStateVariants(sideModels, blockStateVariant -> blockStateVariant.put(VariantSettings.Y, VariantSettings.Rotation.R90)))
                .with(BlockStateModelGenerator.buildBlockStateVariants(sideModels, blockStateVariant -> blockStateVariant.put(VariantSettings.Y, VariantSettings.Rotation.R180)))
                .with(BlockStateModelGenerator.buildBlockStateVariants(sideModels, blockStateVariant -> blockStateVariant.put(VariantSettings.Y, VariantSettings.Rotation.R270))));
    }

    private static Stream<Identifier> getFireModels(BlockStateModelGenerator modelGenerator, Block texture, String midfix) {
        return IntStream.range(0, 2).mapToObj(i -> ModelIds.getBlockSubModelId(texture, midfix + i));
    }
}
