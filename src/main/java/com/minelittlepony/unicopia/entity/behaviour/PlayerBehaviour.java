package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.CapeHolder;
import com.minelittlepony.unicopia.entity.Leaner;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerBehaviour extends EntityBehaviour<PlayerEntity> {
    @Override
    public void update(Caster<?> source, PlayerEntity entity, Disguise spell) {
        if (source instanceof Pony) {
            PlayerEntity pFrom = ((Pony)source).getMaster();

            entity.capeX = pFrom.capeX;
            entity.capeY = pFrom.capeY;
            entity.capeZ = pFrom.capeZ;
            entity.prevCapeX = pFrom.prevCapeX;
            entity.prevCapeY = pFrom.prevCapeY;
            entity.prevCapeZ = pFrom.prevCapeZ;
        } else {
            ((CapeHolder)entity).callUpdateCapeAngles();
        }

        if (source.getEntity().getPose() != entity.getPose()) {
            entity.setPose(source.getEntity().getPose());
        }
        if (source.getEntity().isSwimming() != entity.isSwimming()) {
            entity.setSwimming(source.getEntity().isSwimming());
        }
        if (source.getEntity() instanceof Leaner) {
            ((Leaner)entity).copyFrom(((Leaner)source.getEntity()));
        }
    }
}
