package com.minelittlepony.unicopia.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.server.world.LightSources;

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

        public LightEmitter(T entity) {
            this.entity = entity;
        }

        @SuppressWarnings("deprecation")
        public void tick() {
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
                LightSources.get(entity.getWorld()).addLightSource(entity);

                try {
                    if (lastPos != null) {
                        entity.getWorld().getLightingProvider().checkBlock(lastPos);
                        entity.getWorld().getLightingProvider().checkBlock(currentPos);
                    }
                    lastPos = currentPos;
                } catch (Exception ignored) { }
            }
        }

        public void remove() {
            LightSources.get(entity.getWorld()).removeLightSource(entity);
            if (lastPos != null) {
                try {
                    entity.getWorld().getLightingProvider().checkBlock(lastPos);
                } catch (Exception ignored) {}
            }
        }
    }
}