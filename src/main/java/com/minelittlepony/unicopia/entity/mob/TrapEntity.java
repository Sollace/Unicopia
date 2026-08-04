package com.minelittlepony.unicopia.entity.mob;

import com.minelittlepony.unicopia.entity.Trap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

public abstract class TrapEntity extends Entity implements Trap {

    private long lastDismountAttempt;

    public TrapEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        long now = System.currentTimeMillis();
        if (passenger == this.getFirstPassenger() && ((lastDismountAttempt + 10000) < now || !attemptDismount(passenger))) {
            lastDismountAttempt = now;
            return;
        }
        super.removePassenger(passenger);
    }
}
