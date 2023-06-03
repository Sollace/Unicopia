package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.passive.BeeEntity;

public class BeeBehaviour extends EntityBehaviour<BeeEntity> {
    @Override
    public BeeEntity onCreate(BeeEntity entity, EntityAppearance context, boolean replaceOld) {
        super.onCreate(entity, context, replaceOld);
        if (replaceOld && entity.getWorld().isClient) {
            InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_BEE, entity.getId());
        }
        return entity;
    }

    @Override
    public void update(Living<?> source, BeeEntity entity, Disguise spell) {
        if (source.asEntity().isSneaking()) {
            entity.setAngerTime(10);
        } else {
            entity.setAngerTime(0);
        }
    }
}
