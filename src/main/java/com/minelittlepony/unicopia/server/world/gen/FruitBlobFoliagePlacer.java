package com.minelittlepony.unicopia.server.world.gen;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2i;

import com.minelittlepony.unicopia.block.FruitBearingBlock;
import com.minelittlepony.unicopia.server.world.UWorldGen;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

public class FruitBlobFoliagePlacer extends BlobFoliagePlacer {
    public static final MapCodec<FruitBlobFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(instance -> createCodec(instance).apply(instance, FruitBlobFoliagePlacer::new));

    public FruitBlobFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset, height);
    }

    @Override
    protected FoliagePlacerType<?> getType() {
        return UWorldGen.FRUIT_BLOB_FOLIAGE_PLACER_TYPE;
    }

    @Override
    public void generate(TestableWorld world, BlockPlacer placer, Random random, TreeFeatureConfig config, int trunkHeight, TreeNode treeNode, int foliageHeight, int radius) {
        final Map<Vector2i, Pair<FruitBearingBlock, Integer>> leafPositions = new HashMap<>();
        super.generate(world, new BlockPlacer() {
            @Override
            public void placeBlock(BlockPos pos, BlockState state) {
                placer.placeBlock(pos, state);
                if (state.getBlock() instanceof FruitBearingBlock block
                        && block.isPositionValidForFruit(state, pos)) {
                    leafPositions.compute(new Vector2i(pos.getX(), pos.getZ()), (col, original) -> original == null || original.getSecond() > pos.getY() ? Pair.of(block, pos.getY()) : original);
                }
            }

            @Override
            public boolean hasPlacedBlock(BlockPos pos) {
                return placer.hasPlacedBlock(pos);
            }
        }, random, config, trunkHeight, treeNode, foliageHeight, radius);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        leafPositions.entrySet().forEach(pos -> {
            mutable.set(pos.getKey().x(), pos.getValue().getSecond() - 1, pos.getKey().y());
            if (!placer.hasPlacedBlock(mutable) && random.nextInt(12) == 0) {
                placer.placeBlock(mutable, pos.getValue().getFirst().getPlacedFruitState(random));
            }
        });
    }
}
