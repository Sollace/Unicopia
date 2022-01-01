package com.minelittlepony.unicopia.entity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface DynamicLightSource {
    int getLightLevel();

    static final class LightEmitter<T extends Entity & DynamicLightSource> {
        @Nullable
        private BlockPos lastPos;

        private final T entity;

        LightEmitter(T entity) {
            this.entity = entity;
        }

        @SuppressWarnings("deprecation")
        void tick() {
            if (entity.world.isClient) {
                if (entity.isRemoved()) {
                    remove();
                    return;
                }

                int light = entity.getLightLevel();

                if (light <= 0) {
                    return;
                }

                BlockPos currentPos = entity.getBlockPos();

                if (!currentPos.equals(lastPos) && entity.world.isChunkLoaded(currentPos)) {
                    try {
                        if (lastPos != null) {
                            entity.world.getLightingProvider().checkBlock(lastPos);
                        }
                        entity.world.getLightingProvider().addLightSource(currentPos, light);
                        lastPos = currentPos;
                    } catch (Exception ignored) { }
                }
            }
        }

        void remove() {
            if (entity.world.isClient && lastPos != null) {
                try {
                    entity.world.getLightingProvider().checkBlock(lastPos);
                } catch (Exception ignored) {}
            }
        }
    }
}