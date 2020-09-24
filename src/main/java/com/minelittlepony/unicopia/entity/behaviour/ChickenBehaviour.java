package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.math.Vec3d;

public class ChickenBehaviour extends EntityBehaviour<ChickenEntity> {
    @Override
    public void update(Caster<?> source, ChickenEntity entity) {
        Entity src = source.getEntity();
        Vec3d vel = src.getVelocity();

        if (!src.isOnGround() && vel.y < 0) {
            src.setVelocity(vel.multiply(1, 0.6, 1));
        }
    }
}
