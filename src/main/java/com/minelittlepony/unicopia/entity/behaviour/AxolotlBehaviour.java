package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.util.math.Vec3f;

public class AxolotlBehaviour extends EntityBehaviour<AxolotlEntity> {
    private static final float toRad = 0.017453292F;
    @Override
    public void update(Caster<?> source, AxolotlEntity entity, DisguiseSpell spell) {
        if (entity.getModelAngles().isEmpty()) {
            return;
        }
        Vec3f current = entity.getModelAngles().get("body");
        entity.getModelAngles().put("body", new Vec3f(
               source.getEntity().isSubmergedInWater() ? source.getEntity().getPitch() * toRad : 0,
               0,
               current == null ? 0 : current.getZ()
       ));
    }
}
