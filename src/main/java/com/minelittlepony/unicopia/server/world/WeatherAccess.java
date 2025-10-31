package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.block.cloud.CloudLike;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.Chunk;

public interface WeatherAccess extends WorldView {
    int CLOUD_GENERATION_HEIGHT = 230;
    int CLOUDS_BUFFER_RANGE = 5;
    int FANCY_CLOUDS_BUFFER_RANGE = 10;
    int CHUNK_SECTION_HEIGHT = ChunkSectionPos.field_33097;

    void setWeatherOverride(Float rain, Float thunder);

    default boolean isInRangeOfStorm(BlockPos pos) {
        return WeatherConditions.get(this).isInRangeOfStorm(pos);
    }

    @Environment(EnvType.CLIENT)
    default boolean isBelowClientCloudLayer(BlockPos pos) {

        int range = MinecraftClient.isFancyGraphicsOrBetter() ? FANCY_CLOUDS_BUFFER_RANGE : CLOUDS_BUFFER_RANGE;

        if (pos.getY() < CLOUD_GENERATION_HEIGHT - range) {
            return true;
        }

        Chunk chunk = getChunk(pos);
        int topSection = chunk.getHighestNonEmptySection();

        if (topSection > Chunk.MISSING_SECTION) {
            int sectionBottomY = ChunkSectionPos.getBlockCoord(topSection);
            if (sectionBottomY >= pos.getY() - CHUNK_SECTION_HEIGHT) {
                BlockPos.Mutable mutable = pos.mutableCopy();
                BlockPos.Mutable probeMutable = pos.mutableCopy();
                int maxDistance = CHUNK_SECTION_HEIGHT;

                while (!isOutOfHeightLimit(mutable)) {
                    if (--maxDistance <= 0) break;
                    if (!isAir(probeMutable.setY(mutable.getY() + range))) {

                        mutable.set(pos);
                        maxDistance = CHUNK_SECTION_HEIGHT;

                        while (!isOutOfHeightLimit(mutable)) {
                            if (--maxDistance <= 0) break;
                            if (getBlockState(probeMutable.setY(mutable.getY())).getBlock() instanceof CloudLike) {
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
        if (pos.getY() < CLOUD_GENERATION_HEIGHT) {
            return true;
        }

        Chunk chunk = getChunk(pos);
        int topSection = chunk.getHighestNonEmptySection();

        if (topSection > Chunk.MISSING_SECTION) {
            int sectionBottomY = ChunkSectionPos.getBlockCoord(topSection);
            if (sectionBottomY >= pos.getY() - CHUNK_SECTION_HEIGHT) {
                BlockPos.Mutable mutable = pos.mutableCopy();
                int maxDistance = CHUNK_SECTION_HEIGHT * 2;

                while (!isOutOfHeightLimit(mutable)) {
                    if (--maxDistance <= 0) break;
                    if (!isAir(mutable)) {
                        return true;
                    }
                    mutable.move(Direction.UP);
                }
            }
        }
        return false;
    }
}
