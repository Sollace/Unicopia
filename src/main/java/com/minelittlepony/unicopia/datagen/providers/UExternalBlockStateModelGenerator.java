package com.minelittlepony.unicopia.datagen.providers;

import static net.minecraft.client.data.TextureKey.SIDE;
import static net.minecraft.client.data.TextureKey.TOP;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.EdibleBlock;
import com.minelittlepony.unicopia.datagen.IndirectionUtils;
import com.minelittlepony.unicopia.datagen.IndirectionUtils.IndirectMultipartBlockStateSupplier;

import net.minecraft.client.data.BlockModelDefinitionCreator;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.TextureMap;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;

import static net.minecraft.util.math.AxisRotation.*;

public class UExternalBlockStateModelGenerator extends UBlockStateModelGenerator {
    public UExternalBlockStateModelGenerator(BlockStateModelGenerator modelGenerator, Consumer<BlockModelDefinitionCreator> blockStateCollector) {
        super(blockStateCollector, modelGenerator.itemModelOutput, modelGenerator.modelCollector);
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
                int index = i;
                if (axis == Direction.Axis.X) {
                    index = EdibleBlock.rotate(Direction.Axis.Z, index);
                    index = EdibleBlock.rotate(Direction.Axis.Z, index);
                    index = EdibleBlock.rotate(Direction.Axis.Z, index);
                    index = EdibleBlock.rotate(Direction.Axis.X, index);
                }
                if (axis == Direction.Axis.Z) {
                    index = EdibleBlock.rotate(Direction.Axis.X, index);
                }

                BooleanProperty segment = EdibleBlock.SEGMENTS[index];

                supplier.with(createMultipartConditionBuilder().put(EdibleBlock.AXIS, axis).put(segment, true), createWeightedVariant(createModelVariant(uploadedModels.computeIfAbsent(i, ii -> {
                            return BlockModels.BALE_MODELS[ii].getLeft().upload(blockId.withPath(p -> "block/" + p + BlockModels.BALE_MODELS[ii].getRight()), textures, modelCollector);
                        }))
                        .withRotationX(axis == Direction.Axis.Y ? R0 : axis == Direction.Axis.X ? R90 : R90)
                        .withRotationY(axis == Direction.Axis.Y ? R0 : axis == Direction.Axis.X ? R90 : R0))
                );
            }
        }

        blockStateCollector.accept(supplier);
    }
}
