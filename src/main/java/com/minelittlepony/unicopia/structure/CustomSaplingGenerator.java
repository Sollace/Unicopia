package com.minelittlepony.unicopia.structure;

import java.util.Random;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.minelittlepony.unicopia.block.UBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.decorator.BeehiveTreeDecorator;
import net.minecraft.world.gen.feature.BranchedTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.stateprovider.SimpleStateProvider;

public class CustomSaplingGenerator extends SaplingGenerator {
    public static final CustomSaplingGenerator APPLE_TREE = new CustomSaplingGenerator(5, Blocks.OAK_LOG.getDefaultState(), UBlocks.APPLE_LEAVES.getDefaultState());

    public final BranchedTreeFeatureConfig fancyConfig;
    public final BranchedTreeFeatureConfig fancyHiveConfig;
    public final BranchedTreeFeatureConfig hiveConfig;
    public final BranchedTreeFeatureConfig config;

    public CustomSaplingGenerator(int height, BlockState log, BlockState leaves) {
        fancyConfig = new BranchedTreeFeatureConfig.Builder(
                new SimpleStateProvider(log),
                new SimpleStateProvider(leaves),
                new BlobFoliagePlacer(0, 0))
            .baseHeight(height)
            .build();
        fancyHiveConfig = new BranchedTreeFeatureConfig.Builder(
                new SimpleStateProvider(log),
                new SimpleStateProvider(leaves),
                new BlobFoliagePlacer(0, 0))
            .treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.05F)))
            .baseHeight(height)
            .build();

        hiveConfig = new BranchedTreeFeatureConfig.Builder(
                new SimpleStateProvider(log),
                new SimpleStateProvider(leaves),
                new BlobFoliagePlacer(2, 0))
            .baseHeight(height)
            .heightRandA(height / 2)
            .foliageHeight((height * 2) / 3)
            .noVines()
            .treeDecorators(ImmutableList.of(new BeehiveTreeDecorator(0.05F)))
            .build();

        config = new BranchedTreeFeatureConfig.Builder(
                new SimpleStateProvider(log),
                new SimpleStateProvider(leaves),
                new BlobFoliagePlacer(2, 0))
            .baseHeight(4)
            .heightRandA(2)
            .foliageHeight(3)
            .noVines()
            .build();
    }

    @Override
    @Nullable
    protected ConfiguredFeature<BranchedTreeFeatureConfig, ?> createTreeFeature(Random random, boolean hives) {
        return random.nextInt(10) == 0
                ? Feature.FANCY_TREE.configure(hives ? fancyHiveConfig : fancyConfig)
                : Feature.NORMAL_TREE.configure(hives ? hiveConfig : config);
    }
}
