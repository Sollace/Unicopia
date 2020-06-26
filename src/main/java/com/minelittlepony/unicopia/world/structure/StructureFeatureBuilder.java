package com.minelittlepony.unicopia.world.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.minelittlepony.unicopia.mixin.MixinStructureFeature;
import com.minelittlepony.unicopia.mixin.MixinStructuresConfig;
import com.mojang.serialization.Codec;

import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class StructureFeatureBuilder<T extends StructureFeature<C>, C extends FeatureConfig> {
    private final Identifier id;

    private final Codec<C> codec;

    private final Function<Codec<C>, T> factory;

    private GenerationStep.Feature step;

    private StructureConfig config = new StructureConfig(0, 0, 1);

    private List<ConfiguredBiomes> biomes = new ArrayList<>();

    private final Map<String, StructurePieceType> pieces = new HashMap<>();

    public StructureFeatureBuilder(Identifier id, Codec<C> codec, Function<Codec<C>, T> factory) {
        this.id = id;
        this.codec = codec;
        this.factory = factory;
    }

    public StructureFeatureBuilder<T, C> biomes(C config, Biome...biomes) {
        this.biomes.add(new ConfiguredBiomes(config, biomes));
        return this;
    }

    public StructureFeatureBuilder<T, C> config(int spacing, int separation, int seed) {
        if (spacing >= separation) {
            throw new IllegalArgumentException("Spacing must be less than separation");
        }
        config = new StructureConfig(spacing, separation, seed);
        return this;
    }

    public StructureFeatureBuilder<T, C> step(GenerationStep.Feature step) {
        this.step = step;
        return this;
    }

    public StructureFeatureBuilder<T, C> piece(String name, StructurePieceType piece) {
        if (!pieces.containsValue(piece)) {
            pieces.put(name, piece);
        }
        return this;
    }

    public T build() {
        T feature = factory.apply(codec);

        Map<StructureFeature<?>, StructureConfig> configs = new HashMap<>(StructuresConfig.DEFAULT_STRUCTURES);
        configs.put(feature, config);

        MixinStructuresConfig.setDefaultConfigs(ImmutableMap.copyOf(configs));

        MixinStructureFeature.register(id.toString(), feature, step);

        pieces.forEach((name, piece) -> {
            Registry.register(Registry.STRUCTURE_PIECE, new Identifier(id.getNamespace(), id.getPath() + "/" + name), piece);
        });

        if (!biomes.isEmpty()) {
            biomes.forEach(set -> set.configure(feature));
        }

        return feature;
    }

    class ConfiguredBiomes {
        private final C config;
        private final Biome[] biomes;

        ConfiguredBiomes(C config, Biome[] biomes) {
            this.config = config;
            this.biomes = biomes;
        }

        void configure(T feature) {
            ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configuredFeature = feature.configure(config);
            for (Biome biome : biomes) {
                biome.addStructureFeature(configuredFeature);
            }
        }
    }
}
