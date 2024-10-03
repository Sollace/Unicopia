package com.minelittlepony.unicopia.datagen.providers;

import static net.minecraft.data.client.TextureKey.SIDE;
import static net.minecraft.data.client.TextureKey.TOP;
import static net.minecraft.data.client.VariantSettings.MODEL;
import static net.minecraft.data.client.VariantSettings.X;
import static net.minecraft.data.client.VariantSettings.Y;
import static net.minecraft.data.client.VariantSettings.Rotation.R0;
import static net.minecraft.data.client.VariantSettings.Rotation.R90;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.datagen.IndirectionUtils;
import com.minelittlepony.unicopia.datagen.IndirectionUtils.IndirectMultipartBlockStateSupplier;

import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateSupplier;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.When;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;

public class UExternalBlockStateModelGenerator extends UBlockStateModelGenerator {
    public UExternalBlockStateModelGenerator(BlockStateModelGenerator modelGenerator, Consumer<BlockStateSupplier> blockStateCollector) {
        super(blockStateCollector, modelGenerator.modelCollector, modelGenerator::excludeFromSimpleItemModelGeneration);
    }

    @Override
    public void register() {
        // bales
        registerAll((g, block) -> registerBale(Unicopia.id(block.getLeft().getPath().replace("bale", "block")), block.getLeft(), block.getRight()),
                new Pair<>(Identifier.ofVanilla("hay_block"), "_top"),
                new Pair<>(Identifier.of("farmersdelight", "rice_bale"), "_top"),
                new Pair<>(Identifier.of("farmersdelight", "straw_bale"), "_end")
        );
    }


    public void registerBale(Identifier blockId, Identifier baseBlockId, String endSuffex) {
        Identifier top = baseBlockId.withPath(p -> "block/" + p + endSuffex);
        Identifier side = baseBlockId.withPath(p -> "block/" + p + "_side");
        TextureMap textures = new TextureMap().put(TOP, top).put(SIDE, side);

        IndirectMultipartBlockStateSupplier supplier = IndirectionUtils.multipartBlockStateSupplier(blockId);
        Map<Integer, Identifier> uploadedModels = new HashMap<>();

        for (Direction.Axis axis : Direction.Axis.VALUES) {
            for (int i = 0; i < EdibleBlock.SEGMENTS.length; i++) {
                BooleanProperty segment = EdibleBlock.SEGMENTS[i];
                segment.getName();
                supplier.with(When.create().set(EdibleBlock.AXIS, axis).set(segment, true), BlockStateVariant.create()
                        .put(MODEL, uploadedModels.computeIfAbsent(i, ii -> {
                            return BlockModels.BALE_MODELS[ii].getLeft().upload(blockId.withPath(p -> "block/" + p + BlockModels.BALE_MODELS[ii].getRight()), textures, modelCollector);
                        }))
                        .put(X, axis == Direction.Axis.Y ? R0 : R90)
                        .put(Y, axis == Direction.Axis.X ? R90 : R0)
                );
            }
        }

        blockStateCollector.accept(supplier);
    }
}
