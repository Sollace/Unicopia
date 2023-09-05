package com.minelittlepony.unicopia.entity.mob;

import com.minelittlepony.unicopia.entity.DynamicLightSource;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public abstract class LightEmittingEntity extends Entity implements DynamicLightSource {
    private final LightEmitter<?> emitter = new LightEmitter<>(this);

    public LightEmittingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void tick() {
        super.tick();
        emitter.tick();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        emitter.remove();
    }
}
