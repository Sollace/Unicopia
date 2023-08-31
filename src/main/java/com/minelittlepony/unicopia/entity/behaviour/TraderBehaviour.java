package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.passive.MerchantEntity;

public class TraderBehaviour extends EntityBehaviour<MerchantEntity> {
    @Override
    public void update(Pony pony, MerchantEntity entity, Disguise spell) {
        if (pony.sneakingChanged() && pony.asEntity().isSneaking()) {
            entity.setHeadRollingTimeLeft(40);

            if (!entity.getWorld().isClient()) {
               entity.playSound(USounds.Vanilla.ENTITY_VILLAGER_NO, 1, 1);
            }
        }
    }
}
