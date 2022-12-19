package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WaterCreatureEntity;

public class WaterCreatureBehaviour extends EntityBehaviour<WaterCreatureEntity> {
    @Override
    public void update(Living<?> source, WaterCreatureEntity entity, Disguise spell) {

        if (source.asEntity().isInsideWaterOrBubbleColumn()) {
            source.asEntity().setAir(source.asEntity().getAir() - 1);
            if (source.asEntity().getAir() == -20) {
                source.asEntity().setAir(0);
                source.asEntity().damage(DamageSource.DRYOUT, 2);
            }
        } else {
            source.asEntity().setAir(300);
        }

    }
}
