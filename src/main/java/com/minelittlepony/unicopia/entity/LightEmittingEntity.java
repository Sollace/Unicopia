package com.minelittlepony.unicopia.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface LightEmittingEntity {

    int getLightLevel();

    static class LightEmitter<T extends Entity & LightEmittingEntity> {
        private BlockPos lastPos;

        private final T entity;

        public LightEmitter(T entity) {
            this.entity = entity;
        }

        @SuppressWarnings("deprecation")
        public void tick() {

            if (entity.isRemoved()) {
                remove();
                return;
            }

            if (entity.world.isClient) {
                int light = entity.getLightLevel();

                if (light <= 0) {
                    return;
                }

                BlockPos currentPos = entity.getBlockPos();

                if (entity.world.isChunkLoaded(currentPos)) {
                    try {
                        if (lastPos != null && !currentPos.equals(lastPos)) {
                            entity.world.getLightingProvider().checkBlock(lastPos);
                        }

                        entity.world.getLightingProvider().addLightSource(currentPos, light);
                    } catch (Exception ignored) {
                    }

                    lastPos = currentPos;
                }
            }
        }

        void remove() {
            if (entity.world.isClient) {
                try {
                    entity.world.getLightingProvider().checkBlock(entity.getBlockPos());
                } catch (Exception ignored) {}
            }
        }
    }
}
