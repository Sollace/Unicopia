package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.WaterCreatureEntity;

public class WaterCreatureBehaviour extends EntityBehaviour<WaterCreatureEntity> {
    @Override
    public void update(Caster<?> source, WaterCreatureEntity entity, Disguise spell) {

        if (source.getEntity().isInsideWaterOrBubbleColumn()) {
            source.getEntity().setAir(source.getEntity().getAir() - 1);
            if (source.getEntity().getAir() == -20) {
                source.getEntity().setAir(0);
                source.getEntity().damage(DamageSource.DRYOUT, 2);
            }
        } else {
            source.getEntity().setAir(300);
        }

    }
}
