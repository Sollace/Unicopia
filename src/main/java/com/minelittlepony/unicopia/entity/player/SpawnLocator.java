package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.util.MeteorlogicalUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

public class SpawnLocator extends SpawnLocating {
    private static BlockPos findSafeSpawnLocation(ServerWorld world, int x, int z, boolean avoidAir) {
        if (!avoidAir) {
            return findOverworldSpawn(world, x, z);
        }

        boolean hasCeiling = world.getDimension().hasCeiling();
        WorldChunk chunk = world.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        int startHeight = hasCeiling
                ? world.getChunkManager().getChunkGenerator().getSpawnHeight(world)
                : chunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x & 0xF, z & 0xF);
        if (startHeight < world.getBottomY()) {
            return null;
        }

        int terrainHeight = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 0xF, z & 0xF);
        if (terrainHeight <= startHeight && terrainHeight > chunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR, x & 0xF, z & 0xF)) {
            return null;
        }

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int y = startHeight + 1; y >= world.getBottomY(); --y) {
            mutable.set(x, y, z);
            BlockState state = world.getBlockState(mutable);
            FluidState fluid = state.getFluidState();
            if (fluid.isEmpty()) {
                continue;
            }
            if (!fluid.isIn(FluidTags.WATER)) {
                break;
            }
            if (!Block.isFaceFullSquare(state.getCollisionShape(world, mutable), Direction.UP)) {
                continue;
            }

            return mutable.up().toImmutable();
        }
        return null;
    }

    private static boolean checkAtmosphere(ServerWorld world, BlockPos pos, boolean avoidAir) {
        if (avoidAir) {
            return world.getFluidState(pos).isIn(FluidTags.WATER);

        }
        return world.getFluidState(pos).isEmpty();
    }

    public static void selectSpawnPosition(ServerWorld world, PlayerEntity entity, boolean avoidAir, boolean avoidSun) {
        BlockPos spawnPos = world.getSpawnPos();
        int spawnRadius = Math.min(
                MathHelper.floor(world.getWorldBorder().getDistanceInsideBorder(spawnPos.getX(), spawnPos.getZ())),
                Math.max(0, world.getServer().getSpawnRadius(world))
        );

        long l = spawnRadius * 2 + 1;
        long m = l * l;
        int spawnArea = m > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)m;
        int offsetMultiplier = spawnArea <= 16 ? spawnArea - 1 : 17;
        int rng = Random.create().nextInt(spawnArea);

        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (int attempt = 0; attempt < spawnArea; attempt++) {
            int tile = (rng + offsetMultiplier * attempt) % spawnArea;
            int x = tile % (spawnRadius * 2 + 1);
            int z = tile / (spawnRadius * 2 + 1);

            BlockPos candidatePos = findSafeSpawnLocation(world,
                    spawnPos.getX() + x - spawnRadius,
                    spawnPos.getZ() + z - spawnRadius,
                    avoidAir
            );

            if (candidatePos == null) {
                continue;
            }

            mutable.set(candidatePos);
            mutable.move(0, -1, 0);

            while (!world.isAir(mutable) && mutable.getY() >= spawnPos.getY() - spawnRadius * 2 && !world.isOutOfHeightLimit(mutable)) {
                mutable.move(0, -1, 0);
            }
            while (world.isAir(mutable) && mutable.getY() >= spawnPos.getY() - spawnRadius * 2 && !world.isOutOfHeightLimit(mutable)) {
                mutable.move(0, -1, 0);
            }

            if (!checkAtmosphere(world, mutable, avoidAir)) {
                continue;
            }

            if (!world.isAir(mutable)) {
                mutable.move(0, 1, 0);
            }

            if (!checkAtmosphere(world, mutable, avoidAir)) {
                continue;
            }

            entity.refreshPositionAndAngles(mutable, 0, 0);

            if (!world.isSpaceEmpty(entity)) {
                continue;
            }

            if (avoidSun && MeteorlogicalUtil.isPositionExposedToSun(world, mutable)) {
                continue;
            }

            break;
        }
    }
}
