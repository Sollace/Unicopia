package com.minelittlepony.unicopia.entity.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.util.MeteorlogicalUtil;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

public class SpawnLocator {
    // Modified from SpawnLocating.findOverworldSpawn
    private static BlockPos findOverworldSpawn(ServerWorld world, int x, int z, boolean avoidAir) {
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
            var pass = checkAtmosphere(state.getFluidState(), avoidAir);
            if (pass == TriState.TRUE) {
                continue;
            } else if (pass == TriState.FALSE) {
                return null;
            }

            if (Block.isFaceFullSquare(state.getCollisionShape(world, mutable), Direction.UP)) {
                return mutable.up().toImmutable();
            }
        }
        return null;
    }

    private static BlockPos findAdjustedOverworldSpawn(ServerWorld world, PlayerEntity entity, Box box, BlockPos basePos,
            int x, int z,
            int spawnRadius, boolean avoidAir, boolean avoidSun) {
        BlockPos spawnPos = SpawnLocator.findOverworldSpawn(world, x, z, avoidAir);

        if (spawnPos == null) {
            return null;
        }

        if (avoidSun && MeteorlogicalUtil.isPositionExposedToSun(world, spawnPos)) {
            spawnPos = findUndergroundSpaceBelow(world, entity, basePos, spawnRadius, box, spawnPos);

            if (MeteorlogicalUtil.isPositionExposedToSun(world, spawnPos)) {
                return null;
            }
        }

        if (!checkAtmosphere(world, spawnPos, avoidAir)) {
            return null;
        }

        return spawnPos;
    }

    public static BlockPos findAdjustedOverworldSpawn(ServerWorld world, PlayerEntity entity, Box box, BlockPos basePos, int x, int z, Operation<BlockPos> operation) {
        boolean avoidSun = Pony.of(entity).getCompositeRace().includes(Race.BAT);
        boolean avoidAir = Pony.of(entity).getCompositeRace().includes(Race.SEAPONY);
        if (!(avoidSun || avoidAir)) {
            return operation.call(world, x, z);
        }
        int spawnRadius = Math.max(16, world.getServer().getSpawnRadius(world));
        return findAdjustedOverworldSpawn(world, entity, box, basePos, x, z, spawnRadius, avoidAir, avoidSun);
    }

    private static TriState checkAtmosphere(FluidState state, boolean avoidAir) {
        if (avoidAir) {
            if (state.isEmpty()) {
                return TriState.TRUE;
            }
            if (!state.isIn(FluidTags.WATER)) {
                return TriState.FALSE;
            }
        }

        return state.isEmpty() ? TriState.DEFAULT : TriState.FALSE;
    }

    private static boolean checkAtmosphere(ServerWorld world, BlockPos pos, boolean avoidAir) {
        if (avoidAir) {
            return world.getFluidState(pos).isIn(FluidTags.WATER);
        }
        return world.getFluidState(pos).isEmpty();
    }

    private static BlockPos findUndergroundSpaceBelow(ServerWorld world, PlayerEntity entity, BlockPos basePos, int spawnRadius, Box box, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        mutable.move(0, -1, 0);
        // move to ground
        while (!world.isSpaceEmpty(entity, box.offset(mutable.toBottomCenterPos()))
                && mutable.getY() >= basePos.getY() - spawnRadius * 2
                && mutable.getY() > world.getBottomY() + 1) {
            mutable.move(Direction.DOWN);
        }
        // move down until we find a place we can stand
        while (world.isSpaceEmpty(entity, box.offset(mutable.down().toBottomCenterPos()))
                && mutable.getY() >= basePos.getY() - spawnRadius * 2
                && mutable.getY() > world.getBottomY() + 1) {
            mutable.move(Direction.DOWN);
        }
        return mutable.toImmutable();
    }
}
