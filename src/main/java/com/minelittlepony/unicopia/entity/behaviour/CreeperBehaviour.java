package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;

public class CreeperBehaviour extends EntityBehaviour<CreeperEntity> {
    @Override
    public void update(Caster<?> source, CreeperEntity entity, Spell spell) {
        if (isSneakingOnGround(source)) {
            entity.setFuseSpeed(1);
        } else {
            entity.setFuseSpeed(-1);
            entity.setTarget(null);
            entity.getVisibilityCache().clear();
        }
    }

    protected boolean isSneakingOnGround(Caster<?> source) {
        Entity e = source.getEntity();
        return e.isSneaking() && (e.isOnGround() || !(e instanceof PlayerEntity && ((PlayerEntity)e).abilities.flying));
    }
}
