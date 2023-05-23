package com.minelittlepony.unicopia.server.world;

import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.UBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

public class FernFoliagePlacer extends FoliagePlacer {
    public static final Codec<FernFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> fillFoliagePlacerFields(instance).apply(instance, FernFoliagePlacer::new));
    public static final FoliagePlacerType<FernFoliagePlacer> TYPE = Registry.register(Registries.FOLIAGE_PLACER_TYPE, Unicopia.id("fern_foliage_placer"), new FoliagePlacerType<>(CODEC));

    public FernFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> getType() {
        return TYPE;
    }

    @Override
    protected void generate(TestableWorld world, BiConsumer<BlockPos, BlockState> placer, Random random,
            TreeFeatureConfig config, int trunkHeight, TreeNode node, int foliageHeight, int radius, int offset) {

        BlockPos center = node.getCenter();

        // central leaves blob
        for (int y = offset; y >= offset - foliageHeight; --y) {
            int rad = Math.max(radius + node.getFoliageRadius() - 3 - y / 2, 0);
            generateSquare(world, placer, random, config, center, rad, y, node.isGiantTrunk());
        }

        BlockPos.Mutable pos = new BlockPos.Mutable();
        int fanY = 0;
        for (int outset = 1; outset < 6; outset++) {
            for (int j = 0; j < 2; j++) {
                if (outset < 4) {
                    // diagonal frons
                    for (int z = 0; z < 2; z++) {
                        placeFoliageBlock(world, placer, random, config, pos.set(center, outset, fanY, outset + z));
                        placeFoliageBlock(world, placer, random, config, pos.set(center, outset, fanY, -outset + z));
                        placeFoliageBlock(world, placer, random, config, pos.set(center, -outset + z, fanY, outset));
                        placeFoliageBlock(world, placer, random, config, pos.set(center, -outset + z, fanY, -outset));
                    }
                }
                // adjacent frons
                placeFoliageBlock(world, placer, random, config, pos.set(center, outset, fanY, 0));
                placeFoliageBlock(world, placer, random, config, pos.set(center, 0, fanY, outset));
                placeFoliageBlock(world, placer, random, config, pos.set(center, 0, fanY, -outset));
                placeFoliageBlock(world, placer, random, config, pos.set(center, -outset, fanY, 0));

                if (j != 0 || outset % 2 != 0) break;
                fanY--;
            }

            // fruit
            if (outset == 1) {
                BlockState fruitState = UBlocks.BANANAS.getDefaultState();
                if (random.nextInt(5) == 0) {
                    placer.accept(pos.set(center, outset, fanY - 1, 0), fruitState);
                }
                if (random.nextInt(5) == 0) {
                    placer.accept(pos.set(center, -outset, fanY - 1, 0), fruitState);
                }
                if (random.nextInt(5) == 0) {
                    placer.accept(pos.set(center, 0, fanY - 1, outset), fruitState);
                }
                if (random.nextInt(5) == 0) {
                    placer.accept(pos.set(center, 0, fanY - 1, -outset), fruitState);
                }
            }
        }
    }

    @Override
    public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
        return 1;
    }

    @Override
    protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
        return dx == radius && dz == radius && (random.nextInt(2) == 0 || y == 0);
    }

}
