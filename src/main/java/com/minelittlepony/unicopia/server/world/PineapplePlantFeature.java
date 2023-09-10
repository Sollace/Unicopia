package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.block.PineappleCropBlock;
import com.minelittlepony.unicopia.block.UBlocks;
import com.mojang.serialization.Codec;

import net.minecraft.block.enums.BlockHalf;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class PineapplePlantFeature extends Feature<PineapplePlantFeature.Config> {

    public PineapplePlantFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<Config> context) {
        final StructureWorldAccess world = context.getWorld();

        final Random random = context.getRandom();

        int xOffset = 3;
        int zOffset = 3;

        BlockPos.Mutable mutablePos = context.getOrigin().mutableCopy();
        boolean succeeded = false;
        for (BlockPos position : BlockPos.iterateOutwards(context.getOrigin(), xOffset, 0, zOffset)) {
            if (random.nextFloat() < 0.5) {
                continue;
            }

            findTerrainLevel(world, mutablePos.set(position));

            if (world.isOutOfHeightLimit(mutablePos)
                    || (!world.isAir(mutablePos) && !isReplaceable(world, mutablePos))
                    || !isSoil(world, mutablePos.move(Direction.DOWN))) {
                continue;
            }

            mutablePos.move(Direction.UP);

            int maxAge = UBlocks.PINEAPPLE.getMaxAge();
            int age = random.nextBetween(maxAge - 3, maxAge);

            succeeded = true;
            setBlockState(world, mutablePos, UBlocks.PINEAPPLE.withAge(age).with(PineappleCropBlock.WILD, true));

            if (age >= maxAge) {
                mutablePos.move(Direction.UP);

                if (isReplaceable(world, mutablePos)) {
                    setBlockState(world, mutablePos, UBlocks.PINEAPPLE
                            .withAge(random.nextBetween(maxAge / 2, maxAge))
                            .with(PineappleCropBlock.HALF, BlockHalf.TOP)
                            .with(PineappleCropBlock.WILD, true));
                }
            }
        }

        return succeeded;
    }

    static void findTerrainLevel(StructureWorldAccess world, BlockPos.Mutable mutablePos) {
        if (isReplaceable(world, mutablePos)) {
            do {
                mutablePos.move(Direction.DOWN);
            } while (isReplaceable(world, mutablePos) && !world.isOutOfHeightLimit(mutablePos));

            mutablePos.move(Direction.UP);
        }

        if (!isReplaceable(world, mutablePos)) {
            do {
                mutablePos.move(Direction.UP);
            } while (!isReplaceable(world, mutablePos) && !world.isOutOfHeightLimit(mutablePos));
        }
    }

    static boolean isReplaceable(StructureWorldAccess world, BlockPos pos) {
        return (world.isAir(pos) || world.getBlockState(pos).isIn(BlockTags.REPLACEABLE_BY_TREES)) && world.getBlockState(pos).getFluidState().isEmpty();
    }

    public static class Config implements FeatureConfig {
        public static final Config INSTANCE = new Config();
        public static final Codec<Config> CODEC = Codec.unit(INSTANCE);
    }
}
