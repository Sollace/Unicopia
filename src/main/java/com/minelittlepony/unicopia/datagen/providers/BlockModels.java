package com.minelittlepony.unicopia.datagen.providers;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.block.Block;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.ModelIds;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureKey;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.TexturedModel;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public interface BlockModels {
    TextureKey SHELL = TextureKey.of("shell");
    TextureKey STEP = TextureKey.of("step");

    Model FRUIT = block("fruit", TextureKey.CROSS);
    Model STRAIGHT_STAIRS = block("seethrough_stairs", TextureKey.BOTTOM, TextureKey.TOP, TextureKey.SIDE, STEP);
    Model INNER_STAIRS = block("inner_seethrough_stairs", "_inner", TextureKey.BOTTOM, TextureKey.TOP, TextureKey.SIDE, STEP);
    Model OUTER_STAIRS = block("outer_seethrough_stairs", "_outer", TextureKey.BOTTOM, TextureKey.TOP, TextureKey.SIDE, STEP);

    Factory CROP = Factory.of(TextureMap::crop, Models.CROP);
    Factory CUBE_ALL = Factory.of(TextureMap::all, Models.CUBE_ALL);
    TexturedModel.Factory SPIKES = TexturedModel.makeFactory(b -> TextureMap.crop(ModelIds.getBlockModelId(b)), Models.CROP);

    String[] FLATTENED_MODEL_SUFFEXES =     {"xyz", "yz", "xy", "y", "xz", "z", "x", "full"};
    String[] FLATTENED_MODEL_SUFFEXES_ROT = {"xyz", "xy", "yz", "y", "xz", "x", "z", "full"};
    VariantSettings.Rotation[] FLATTENED_MODEL_ROTATIONS = {
            VariantSettings.Rotation.R0, VariantSettings.Rotation.R270, VariantSettings.Rotation.R90, VariantSettings.Rotation.R180,
            VariantSettings.Rotation.R270, VariantSettings.Rotation.R180, VariantSettings.Rotation.R0, VariantSettings.Rotation.R90
    };

    Model[] FLATTENED_MODELS = Arrays.stream(FLATTENED_MODEL_SUFFEXES)
            .map(variant -> block("flattened_corner_" + variant, "_corner_" + variant, TextureKey.ALL))
            .toArray(Model[]::new);
    Model[] SHELL_MODELS = IntStream.range(1, 5)
            .mapToObj(i -> block("template_shell_" + i, "_" + i, SHELL))
            .toArray(Model[]::new);
    Model[] PIE_MODELS = Stream.of("_full", "_elbow", "_straight", "_corner")
            .map(variant -> block("pie" + variant, variant, TextureKey.TOP, TextureKey.BOTTOM, TextureKey.SIDE, TextureKey.INSIDE))
            .toArray(Model[]::new);
    @SuppressWarnings("unchecked")
    Pair<Model, String>[] BALE_MODELS = Stream.of("bnw", "bne", "bsw", "bse", "tnw", "tne", "tsw", "tse")
            .map(suffex -> new Pair<>(block("template_bale_" + suffex, "_" + suffex, TextureKey.TOP, TextureKey.SIDE), "_" + suffex))
            .toArray(Pair[]::new);

    static Model block(String parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Unicopia.id("block/" + parent)), Optional.empty(), requiredTextureKeys);
    }

    static Model block(String parent, String variant, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(Unicopia.id("block/" + parent)), Optional.of(variant), requiredTextureKeys);
    }

    static Model block(Identifier parent, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(parent.withPrefixedPath("block/")), Optional.empty(), requiredTextureKeys);
    }

    static Model block(Identifier parent, String variant, TextureKey ... requiredTextureKeys) {
        return new Model(Optional.of(parent.withPrefixedPath("block/")), Optional.of(variant), requiredTextureKeys);
    }

    public interface Factory {
        static Factory of(Function<Identifier, TextureMap> textureFunc, Model model) {
            return (block, suffix) -> TexturedModel.makeFactory(b -> textureFunc.apply(ModelIds.getBlockSubModelId(b, suffix)), model).get(block);
        }

        TexturedModel get(Block block, String suffix);

        default Identifier upload(Block block, String suffix, BiConsumer<Identifier, Supplier<JsonElement>> writer) {
            return get(block, suffix).upload(block, suffix, writer);
        }
    }
}
