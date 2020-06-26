package com.minelittlepony.unicopia.world.structure;

import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class StructureRegistry {
    public static <T extends StructureFeature<C>, C extends FeatureConfig> StructureFeatureBuilder<T, C> start(Identifier id, Codec<C> codec, Function<Codec<C>, T> factory) {
        return new StructureFeatureBuilder<>(id, codec, factory);
    }

}
