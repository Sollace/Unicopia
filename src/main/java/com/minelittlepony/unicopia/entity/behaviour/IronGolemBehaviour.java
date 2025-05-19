package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class IronGolemBehaviour extends MobBehaviour<IronGolemEntity> {
    @Override
    public void update(Pony player, IronGolemEntity entity, Disguise spell) {
        super.update(player, entity, spell);
        boolean hasPoppy = player.asEntity().getStackInHand(Hand.MAIN_HAND).isOf(Items.POPPY);
        if (hasPoppy != entity.getLookingAtVillagerTicks() > 0) {
            entity.setLookingAtVillager(hasPoppy);
        }
    }
}
