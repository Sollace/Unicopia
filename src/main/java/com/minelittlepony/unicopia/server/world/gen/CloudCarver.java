package com.minelittlepony.unicopia.server.world.gen;

import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.minelittlepony.unicopia.block.UBlocks;
import com.mojang.serialization.Codec;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.carver.CarverContext;
import net.minecraft.world.gen.carver.CarvingMask;
import net.minecraft.world.gen.carver.CaveCarver;
import net.minecraft.world.gen.carver.CaveCarverConfig;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction.NoisePos;

public class CloudCarver extends CaveCarver {

    private Random random;

    private final LongSet topWrittenPositions = new LongOpenHashSet();

    public CloudCarver(Codec<CaveCarverConfig> codec) {
        super(codec);
    }

    @Override
    protected int getMaxCaveCount() {
        return 15;
    }

    @Override
    protected float getTunnelSystemWidth(Random random) {
        return (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
    }

    @Override
    protected double getTunnelSystemHeightWidthRatio() {
        return 0.5;
    }

    @Override
    public boolean carve(
            CarverContext context,
            CaveCarverConfig config,
            Chunk chunk,
            Function<BlockPos, RegistryEntry<Biome>> function,
            Random random,
            AquiferSampler sampler,
            ChunkPos chunkPos,
            CarvingMask carvingMask
        ) {
        this.random = random;
        boolean result = super.carve(context, config, chunk, function, random, new AquiferSampler() {
            @Override
            public BlockState apply(NoisePos pos, double density) {
                BlockState state = sampler.apply(pos, density);
                return state != null && state.isAir() ? UBlocks.CLOUD.getDefaultState() : state;
            }

            @Override
            public boolean needsFluidTick() {
                return sampler.needsFluidTick();
            }

        }, chunkPos, carvingMask);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        topWrittenPositions.forEach(l -> {
            processSurfaceBlocks(mutable.set(l), context, config, chunk, random);
        });
        topWrittenPositions.clear();
        return result;
    }

    @Override
    protected void carveCave(
        CarverContext context,
        CaveCarverConfig config,
        Chunk chunk,
        Function<BlockPos, RegistryEntry<Biome>> posToBiome,
        AquiferSampler aquiferSampler,
        double x,
        double y,
        double z,
        float xScale,
        double yScale,
        CarvingMask mask,
        Carver.SkipPredicate skipPredicate
    ) {
        if (random == null) {
            return;
        }
        int maxY = context.getMinY() + context.getHeight();

        int bubbleCount = 10 + random.nextInt(12);
        for (int i = 0; i < bubbleCount; i++) {
            double width = 1.5 * xScale + random.nextTriangular(3, 2);
            double height = Math.min(width * yScale * (1 + random.nextFloat() * 2) + MathHelper.sin((float) (Math.PI / 2)) * xScale, maxY - y);
            double bubbleX = x + (random.nextFloat() * 2 - 1) * width;
            double bubbleZ = z + (random.nextFloat() * 2 - 1) * width;
            carveRegion(context, config, chunk, posToBiome, aquiferSampler, bubbleX + 1.0, y, bubbleZ, width, height, mask, skipPredicate);
        }
    }

    @Override
    protected void carveTunnels(
            CarverContext context,
            CaveCarverConfig config,
            Chunk chunk,
            Function<BlockPos, RegistryEntry<Biome>> posToBiome,
            long seed,
            AquiferSampler aquiferSampler,
            double x,
            double y,
            double z,
            double horizontalScale,
            double verticalScale,
            float w,
            float yaw,
            float pitch,
            int branchStartIndex,
            int branchCount,
            double yawPitchRatio,
            CarvingMask mask,
            Carver.SkipPredicate skipPredicate
        ) {
        if (random == null) {
            return;
        }
        int maxY = context.getMinY() + context.getHeight();
        int bubbleCount = 10 + random.nextInt(12);
        for (int i = 0; i < bubbleCount; i++) {
            double width = /*1.5 + MathHelper.sin((float) (Math.PI / 2)) * xScale +*/ 1.5 * horizontalScale + random.nextInt(3) + w;
            double height = width * (1 + random.nextFloat() * 2) * verticalScale * 0.2;
            double bubbleX = x + (random.nextFloat() * 2 - 1) * width * 1.5;
            double bubbleZ = z + (random.nextFloat() * 2 - 1) * width * 1.5;
            double bubbleY = y + random.nextFloat() * height * 0.5;
            if (bubbleY + height < maxY) {
                carveRegion(context, config, chunk, posToBiome, aquiferSampler, bubbleX, bubbleY, bubbleZ, width, height, mask, skipPredicate);
            }
        }
        //super.carveTunnels(context, config, chunk, posToBiome, seed, aquiferSampler, x, y, z, horizontalScale, verticalScale, w, yaw, pitch, branchStartIndex, branchCount, yawPitchRatio, mask, skipPredicate);
    }

    @Override
    protected boolean carveAtPoint(
            CarverContext context,
            CaveCarverConfig config,
            Chunk chunk,
            Function<BlockPos, RegistryEntry<Biome>> posToBiome,
            CarvingMask mask,
            BlockPos.Mutable pos,
            BlockPos.Mutable tmp,
            AquiferSampler aquiferSampler,
            MutableBoolean replacedGrassy
        ) {
        if (super.carveAtPoint(context, config, chunk, posToBiome, mask, pos, tmp, aquiferSampler, replacedGrassy)) {
            topWrittenPositions.remove(tmp.set(pos).move(Direction.DOWN).asLong());
            topWrittenPositions.add(pos.asLong());
            if (chunk.getBlockState(tmp).isOf(UBlocks.SOGGY_CLOUD)) {
                chunk.setBlockState(tmp, UBlocks.CLOUD.getDefaultState(), false);
            }
            return true;
        }
        return false;
    }

    protected void processSurfaceBlocks(BlockPos.Mutable pos, CarverContext context, CaveCarverConfig config, Chunk chunk, Random random) {
        if (chunk.getBlockState(pos.move(Direction.UP)).isAir()) {
            chunk.setBlockState(pos.move(Direction.DOWN), UBlocks.SOGGY_CLOUD.getDefaultState(), false);
        }
    }
}