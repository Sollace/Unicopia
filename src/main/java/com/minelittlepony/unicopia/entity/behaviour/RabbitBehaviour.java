package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.RabbitEntity;

public class RabbitBehaviour extends EntityBehaviour<RabbitEntity> {
    @Override
    public void update(Pony player, RabbitEntity entity, DisguiseSpell spell) {

        if (player.getEntity().isOnGround()) {
            if (Entity.squaredHorizontalLength(player.getEntity().getVelocity()) > 0.01) {
                player.getOwner().jump();
                entity.startJump();
            }
        } else if (player.landedChanged()) {
            entity.startJump();
        }
    }
}
