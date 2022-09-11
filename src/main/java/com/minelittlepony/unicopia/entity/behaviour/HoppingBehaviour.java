package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.RabbitEntity;

public class HoppingBehaviour extends EntityBehaviour<LivingEntity> {
    @Override
    public void update(Pony player, LivingEntity entity, Disguise spell) {

        if (player.getEntity().isOnGround()) {
            if (player.getEntity().getVelocity().horizontalLengthSquared() > 0.01) {
                player.getMaster().jump();
                startJump(entity);
            }
        } else if (player.landedChanged()) {
            startJump(entity);
        }
    }

    private void startJump(LivingEntity entity) {
        if (entity instanceof RabbitEntity rabbit) {
            rabbit.startJump();
        }
    }
}
