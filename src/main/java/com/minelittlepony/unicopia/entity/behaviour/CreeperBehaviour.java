package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;

import net.minecraft.entity.mob.CreeperEntity;

public class CreeperBehaviour extends EntityBehaviour<CreeperEntity> {
    @Override
    public void update(Caster<?> source, CreeperEntity entity, DisguiseSpell spell) {
        if (isSneakingOnGround(source)) {
            entity.setFuseSpeed(1);
        } else {
            entity.setFuseSpeed(-1);
            entity.setTarget(null);
            entity.getVisibilityCache().clear();
        }
    }
}
