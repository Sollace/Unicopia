package com.minelittlepony.unicopia.world.structure;

import java.util.Random;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.sapling.SaplingGenerator;
import net.minecraft.world.gen.feature.AbstractTreeFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.OakTreeFeature;

public class CustomSaplingGenerator extends SaplingGenerator {

    private final int height;

    private final BlockState log;
    private final BlockState leaves;

    public CustomSaplingGenerator(int height, BlockState log, BlockState leaves) {
        this.height = height;
        this.log = log;
        this.leaves = leaves;
    }

    @Override
    @Nullable
    protected AbstractTreeFeature<DefaultFeatureConfig> createTreeFeature(Random random) {
       return new OakTreeFeature(DefaultFeatureConfig::deserialize, true, height, log, leaves, false);
    }
 }
