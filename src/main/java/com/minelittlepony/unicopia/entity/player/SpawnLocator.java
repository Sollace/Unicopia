package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;

import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

public class SpawnLocator extends SpawnLocating {

    public static BlockPos findSafeSpawnLocation(ServerWorld world, int x, int z) {
        return SpawnLocating.findOverworldSpawn(world, x, z);
    }

    public static Optional<BlockPos> fuzz(ServerWorld world, BlockPos pos, int horizontal, int vertical) {

        for (int attempt = 0; attempt < 6; attempt++) {
            BlockPos target = FuzzyPositions.localFuzz(world.random, horizontal, vertical);
            target = findSafeSpawnLocation(world, target.getX(), target.getZ());

            if (target != null) {
                return Optional.of(target);
            }
        }

        return Optional.empty();
    }

    public static void selectSpawnPosition(ServerWorld world, PlayerEntity entity) {
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
                    spawnPos.getZ() + z - spawnRadius
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

            if (!world.getFluidState(mutable).isEmpty()) {
                continue;
            }

            if (!world.isAir(mutable)) {
                mutable.move(0, 1, 0);
            }

            if (!world.getFluidState(mutable).isEmpty()) {
                continue;
            }

            entity.refreshPositionAndAngles(mutable, 0, 0);

            if (!world.isSpaceEmpty(entity) || MeteorlogicalUtil.isPositionExposedToSun(world, mutable)) {
                continue;
            }

            break;
        }
    }
}
