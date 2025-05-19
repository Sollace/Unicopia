package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.block.cloud.CloudLike;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public interface WeatherAccess {
    void setWeatherOverride(Float rain, Float thunder);

    default boolean isInRangeOfStorm(BlockPos pos) {
        return WeatherConditions.get((World)this).isInRangeOfStorm(pos);
    }

    @Environment(EnvType.CLIENT)
    default boolean isBelowClientCloudLayer(BlockPos pos) {

        int range = MinecraftClient.isFancyGraphicsOrBetter() ? 10 : 5;

        if (pos.getY() < 230 - range) {
            return true;
        }

        Chunk chunk = ((World)this).getChunk(pos);
        int topSection = chunk.getHighestNonEmptySection();

        if (topSection > -1) {
            int sectionBottomY = ChunkSectionPos.getBlockCoord(topSection);
            if (sectionBottomY >= pos.getY() - 16) {
                BlockPos.Mutable mutable = pos.mutableCopy();
                BlockPos.Mutable probeMutable = pos.mutableCopy();
                int maxDistance = 16;

                while (((World)this).isInBuildLimit(mutable)) {
                    if (--maxDistance <= 0) break;
                    if (!((World)this).isAir(probeMutable.setY(mutable.getY() + range))) {

                        mutable.set(pos);
                        maxDistance = 16;

                        while (((World)this).isInBuildLimit(mutable)) {
                            if (--maxDistance <= 0) break;
                            if (((World)this).getBlockState(probeMutable.setY(mutable.getY())).getBlock() instanceof CloudLike) {
                                return false;
                            }
                            mutable.move(Direction.DOWN);
                        }

                        return true;
                    }
                    mutable.move(Direction.UP);
                }
            }
        }
        return false;
    }

    default boolean isBelowCloudLayer(BlockPos pos) {
        if (pos.getY() < 230) {
            return true;
        }

        Chunk chunk = ((World)this).getChunk(pos);
        int topSection = chunk.getHighestNonEmptySection();

        if (topSection > -1) {
            int sectionBottomY = ChunkSectionPos.getBlockCoord(topSection);
            if (sectionBottomY >= pos.getY() - 16) {
                BlockPos.Mutable mutable = pos.mutableCopy();
                int maxDistance = 32;

                while (((World)this).isInBuildLimit(mutable)) {
                    if (--maxDistance <= 0) break;
                    if (!((World)this).isAir(mutable)) {
                        return true;
                    }
                    mutable.move(Direction.UP);
                }
            }
        }
        return false;
    }
}
