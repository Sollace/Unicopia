package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;

import net.minecraft.entity.passive.BeeEntity;

public class BeeBehaviour extends EntityBehaviour<BeeEntity> {
    @Override
    public BeeEntity onCreate(BeeEntity entity, Disguise context, boolean replaceOld) {
        super.onCreate(entity, context, replaceOld);
        if (replaceOld && entity.world.isClient) {
            InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_BEE);
        }
        return entity;
    }

    @Override
    public void update(Caster<?> source, BeeEntity entity, DisguiseSpell spell) {
        if (source.getMaster().isSneaking()) {
            entity.setAngerTime(10);
        } else {
            entity.setAngerTime(0);
        }
    }
}
