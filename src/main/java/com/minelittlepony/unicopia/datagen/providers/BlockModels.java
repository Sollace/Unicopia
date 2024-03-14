package com.minelittlepony.unicopia.datagen.providers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateSupplier;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.MultipartBlockStateSupplier;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.When;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

public interface BlockModels {
    Model FRUIT = block("fruit", TextureKey.CROSS);

    String[] FLATTENED_MODEL_SUFFEXES =     {"xyz", "yz", "xy", "y", "xz", "z", "x", "full"};
    String[] FLATTENED_MODEL_SUFFEXES_ROT = {"xyz", "xy", "yz", "y", "xz", "x", "z", "full"};
    VariantSettings.Rotation[] FLATTENED_MODEL_ROTATIONS = {
            VariantSettings.Rotation.R0, VariantSettings.Rotation.R270, VariantSettings.Rotation.R90, VariantSettings.Rotation.R180,
            VariantSettings.Rotation.R270, VariantSettings.Rotation.R180, VariantSettings.Rotation.R0, VariantSettings.Rotation.R90
    };

    List<Model> FLATTENED_MODELS = Arrays.stream(FLATTENED_MODEL_SUFFEXES)
            .map(suffex -> "_corner_" + suffex)
            .map(variant -> block("flattened" + variant, variant, TextureKey.ALL))
            .toList();

    static Model block(String parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Unicopia.id("block/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    static Model block(String parent, String variant, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Unicopia.id("block/" + parent)), Optional.of(variant), requiredTextureKeys);
    }

    static Model block(Identifier parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(parent.withPrefixedPath("block/")), Optional.empty(), requiredTextureKeys);
    }

    static void registerCompactedBlock(BlockStateModelGenerator modelGenerator, Block block) {
        FLATTENED_MODELS.forEach(model -> {
            model.upload(block, TextureMap.all(ModelIds.getBlockModelId(block).withPath(p -> p.replace("compacted_", ""))), modelGenerator.modelCollector);
        });
        modelGenerator.blockStateCollector.accept(createCompactedBlockState(block));
    }

    static BlockStateSupplier createCompactedBlockState(Block block) {
        MultipartBlockStateSupplier supplier = MultipartBlockStateSupplier.create(block);
        for (byte i = 0; i < FLATTENED_MODEL_ROTATIONS.length; i++) {
            final BooleanProperty yAxis = (i & 0b100) == 0 ? Properties.DOWN : Properties.UP;
            final BooleanProperty xAxis = (i & 0b010) == 0 ? Properties.NORTH: Properties.SOUTH;
            final BooleanProperty zAxis = (i & 0b001) == 0 ? Properties.EAST : Properties.WEST;
            final VariantSettings.Rotation xRot = yAxis == Properties.DOWN ? VariantSettings.Rotation.R0 : VariantSettings.Rotation.R180;
            final VariantSettings.Rotation yRot = FLATTENED_MODEL_ROTATIONS[i];
            final String[] suffexes = yRot.ordinal() % 2 == 0 ? FLATTENED_MODEL_SUFFEXES : FLATTENED_MODEL_SUFFEXES_ROT;
            for (byte v = 0; v < suffexes.length; v++) {
                supplier.with(When.create()
                            .set(yAxis, (v & 0b100) != 0)
                            .set(xAxis, (v & 0b010) != 0)
                            .set(zAxis, (v & 0b001) != 0), BlockStateVariant.create()
                        .put(VariantSettings.MODEL, ModelIds.getBlockSubModelId(block, "_corner_" + suffexes[v]))
                        .put(VariantSettings.UVLOCK, true)
                        .put(VariantSettings.X, xRot)
                        .put(VariantSettings.Y, yRot)
                );
            }
        }
        return supplier;
    }
}
