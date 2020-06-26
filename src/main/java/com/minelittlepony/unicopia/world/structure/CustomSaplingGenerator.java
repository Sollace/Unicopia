package com.minelittlepony.unicopia.world.structure;

import java.util.Random;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.minelittlepony.unicopia.world.block.UBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.decorator.BeehiveTreeDecorator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.stateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;

public class CustomSaplingGenerator extends SaplingGenerator {
    public static final CustomSaplingGenerator APPLE_TREE = new CustomSaplingGenerator(5, Blocks.OAK_LOG.getDefaultState(), UBlocks.APPLE_LEAVES.getDefaultState());

    public final TreeFeatureConfig fancyConfig;
    public final TreeFeatureConfig fancyHiveConfig;
    public final TreeFeatureConfig hiveConfig;
    public final TreeFeatureConfig config;

    public CustomSaplingGenerator(int height, BlockState log, BlockState leaves) {
        fancyConfig = new TreeFeatureConfig.Builder(
                new SimpleBlockStateProvider(log),
                new SimpleBlockStateProvider(leaves),
                new BlobFoliagePlacer(2, 0, 0, 0, 3),
                new StraightTrunkPlacer(height, 2, 0),
                new TwoLayersFeatureSize(1, 0, 1))
            .build();
        fancyHiveConfig = new TreeFeatureConfig.Builder(
                new SimpleBlockStateProvider(log),
                new SimpleBlockStateProvider(leaves),
                new BlobFoliagePlacer(2, 0, 0, 0, 3),
                new StraightTrunkPlacer(height, 2, 0),
                new TwoLayersFeatureSize(1, 0, 1))
            .decorators(ImmutableList.of(new BeehiveTreeDecorator(0.05F)))
            .build();

        hiveConfig = new TreeFeatureConfig.Builder(
                new SimpleBlockStateProvider(log),
                new SimpleBlockStateProvider(leaves),
                new BlobFoliagePlacer(2, 0, 0, 0, 3),
                new StraightTrunkPlacer(height, height / 2, (height * 2) / 3),
                new TwoLayersFeatureSize(1, 0, 1))
            .ignoreVines()
            .decorators(ImmutableList.of(new BeehiveTreeDecorator(0.05F)))
            .build();

        config = new TreeFeatureConfig.Builder(
                new SimpleBlockStateProvider(log),
                new SimpleBlockStateProvider(leaves),
                new BlobFoliagePlacer(2, 0, 0, 0, 3),
                new StraightTrunkPlacer(height, height / 2, (height * 2) / 3),
                new TwoLayersFeatureSize(1, 0, 1))
            .ignoreVines()
            .build();
    }

    @Override
    @Nullable
    protected ConfiguredFeature<TreeFeatureConfig, ?> createTreeFeature(Random random, boolean hives) {
        return Feature.TREE.configure(random.nextInt(10) == 0
                ? (hives ? fancyHiveConfig : fancyConfig)
                : (hives ? hiveConfig : config)
        );
    }
}
