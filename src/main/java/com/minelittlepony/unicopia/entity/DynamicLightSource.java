package com.minelittlepony.unicopia.entity;

import java.lang.ref.WeakReference;

import com.minelittlepony.unicopia.server.world.LightSources;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface DynamicLightSource {
    default int getLightLevel() {
        return 0;
    }

    default BlockPos getLightSourcePosition() {
        return ((Entity)this).getBlockPos();
    }

    static final class LightEmitter<T extends Entity & DynamicLightSource> {
        private final T entity;
        private WeakReference<World> lastWorld = new WeakReference<>(null);

        public LightEmitter(T entity) {
            this.entity = entity;
        }

        public void tick() {
            if (entity.isRemoved() || entity.getLightLevel() <= 0) {
                remove();
                return;
            }

            World world = entity.getWorld();
            if (!lastWorld.refersTo(world)) {
                remove();
                lastWorld = new WeakReference<>(world);
            }

            LightSources.get(world).addLightSource(entity);
        }

        public void remove() {
            World world = lastWorld.get();
            if (world != null) {
                LightSources.get(world).removeLightSource(entity.getUuid());
                lastWorld.clear();
            }
        }
    }
}