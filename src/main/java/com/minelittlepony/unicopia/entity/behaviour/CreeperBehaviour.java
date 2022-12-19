package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.MathHelper;

public class CreeperBehaviour extends EntityBehaviour<CreeperEntity> {
    @Override
    public void update(Living<?> source, CreeperEntity entity, Disguise spell) {
        int fuseCountDown = spell.getDisguise().getOrCreateTag().getInt("fuseCountdown");

        boolean trigger = isSneakingOnGround(source);

        if (trigger) {
            fuseCountDown++;
        } else if (fuseCountDown > 0) {
            fuseCountDown--;
        }

        int max = source.isClient() ? 90 : 30;

        fuseCountDown = MathHelper.clamp(fuseCountDown, 0, max + 1);

        if (fuseCountDown <= max && trigger) {
            entity.setFuseSpeed(1);
        } else {
            entity.setFuseSpeed(-1);
            entity.setTarget(null);
            entity.getVisibilityCache().clear();
        }

        spell.getDisguise().getOrCreateTag().putInt("fuseCountdown", fuseCountDown);
    }
}
