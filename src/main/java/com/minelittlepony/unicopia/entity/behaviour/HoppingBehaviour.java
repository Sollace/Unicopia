package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.RabbitEntity;

public class HoppingBehaviour extends EntityBehaviour<LivingEntity> {
    @Override
    public void update(Pony player, LivingEntity entity, DisguiseSpell spell) {

        if (player.getEntity().isOnGround()) {
            if (player.getEntity().getVelocity().horizontalLengthSquared() > 0.01) {
                player.getMaster().jump();
                if (entity instanceof RabbitEntity) {
                    ((RabbitEntity)entity).startJump();
                }
            }
        } else if (player.landedChanged()) {
            if (entity instanceof RabbitEntity) {
                ((RabbitEntity)entity).startJump();
            }
        }
    }
}
