package com.minelittlepony.unicopia.entity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface DynamicLightSource {
    default int getLightLevel() {
        return 0;
    }

    static final class LightEmitter<T extends Entity & DynamicLightSource> {
        @Nullable
        private BlockPos lastPos;

        private final T entity;

        LightEmitter(T entity) {
            this.entity = entity;
        }

        @SuppressWarnings("deprecation")
        void tick() {
            if (entity.getWorld().isClient) {
                if (entity.isRemoved()) {
                    remove();
                    return;
                }

                int light = entity.getLightLevel();

                if (light <= 0) {
                    return;
                }

                BlockPos currentPos = entity.getBlockPos();

                if (!currentPos.equals(lastPos) && entity.getWorld().isChunkLoaded(currentPos)) {
                    try {
                        if (lastPos != null) {
                            entity.getWorld().getLightingProvider().checkBlock(lastPos);
                        }
                        // TODO: store this in the ether and inject into Chunk#forEachLightSource
                        //entity.getWorld().getLightingProvider().addLightSource(currentPos, light);
                        lastPos = currentPos;
                    } catch (Exception ignored) { }
                }
            }
        }

        void remove() {
            if (entity.getWorld().isClient && lastPos != null) {
                try {
                    entity.getWorld().getLightingProvider().checkBlock(lastPos);
                } catch (Exception ignored) {}
            }
        }
    }
}