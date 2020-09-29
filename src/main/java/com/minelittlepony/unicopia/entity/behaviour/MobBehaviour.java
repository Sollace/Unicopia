package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.RayTraceHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public class MobBehaviour extends EntityBehaviour<MobEntity> {
    @Override
    public void update(Pony player, MobEntity entity, DisguiseSpell spell) {

        if (player.sneakingChanged() && isSneakingOnGround(player)) {
            entity.tryAttack(RayTraceHelper.findEntity(player.getEntity(), 6, 1, e -> e instanceof LivingEntity).orElse(entity));
        }
    }
}
