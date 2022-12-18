package com.minelittlepony.unicopia.entity.behaviour;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.entity.passive.AxolotlEntity;

public class AxolotlBehaviour extends EntityBehaviour<AxolotlEntity> {
    private static final float toRad = 0.017453292F;
    @Override
    public void update(Caster<?> source, AxolotlEntity entity, Disguise spell) {
        if (entity.getModelAngles().isEmpty()) {
            return;
        }
        Vector3f current = entity.getModelAngles().get("body");
        entity.getModelAngles().put("body", new Vector3f(
               source.getEntity().isSubmergedInWater() ? source.getEntity().getPitch() * toRad : 0,
               0,
               current == null ? 0 : current.z
       ));
    }
}
