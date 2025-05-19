package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.entity.passive.CamelEntity;

public class CamelBehaviour extends EntityBehaviour<CamelEntity> {
    @Override
    public void update(Pony player, CamelEntity entity, Disguise spell) {
        entity.setDashing(player.asEntity().isSprinting());

        if (player.asEntity().isSneaking()) {
            entity.startSitting();
        } else {
            entity.startStanding();
        }
    }
}
