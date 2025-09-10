package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;

import net.minecraft.entity.passive.AxolotlEntity;

public class AxolotlBehaviour extends EntityBehaviour<AxolotlEntity> {
    @Override
    public void update(Living<?> source, AxolotlEntity entity, Disguise spell) {
        // TODO:
        /*
        if (entity.getModelAngles().isEmpty()) {
            return;
        }
        AxolotlEntityRenderer s;
        Vector3f current = entity.getModelAngles().get("body");
        entity.getModelAngles().put("body", new Vector3f(
               source.asEntity().isSubmergedInWater() ? source.asEntity().getPitch() * MathHelper.RADIANS_PER_DEGREE : 0,
               0,
               current == null ? 0 : current.z
       ));*/
    }
}
