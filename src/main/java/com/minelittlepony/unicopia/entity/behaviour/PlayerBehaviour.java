package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.*;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerBehaviour extends EntityBehaviour<PlayerEntity> {
    @Override
    public void update(Living<?> source, PlayerEntity entity, Disguise spell) {
        if (source instanceof Pony pony) {
            PlayerEntity pFrom = pony.asEntity();

            entity.capeX = pFrom.capeX;
            entity.capeY = pFrom.capeY;
            entity.capeZ = pFrom.capeZ;
            entity.prevCapeX = pFrom.prevCapeX;
            entity.prevCapeY = pFrom.prevCapeY;
            entity.prevCapeZ = pFrom.prevCapeZ;
        } else {
            ((PlayerEntityDuck)entity).callUpdateCapeAngles();
        }

        if (source.asEntity().getPose() != entity.getPose()) {
            entity.setPose(source.asEntity().getPose());
        }
        if (source.asEntity().isSwimming() != entity.isSwimming()) {
            entity.setSwimming(source.asEntity().isSwimming());
        }
        if (source.asEntity() instanceof LivingEntityDuck duck) {
            // TODO: CopyAngles
            duck.copyLeaningAnglesFrom(((LivingEntityDuck)entity));
        }
    }
}
