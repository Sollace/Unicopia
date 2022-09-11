package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class EndermanBehaviour extends EntityBehaviour<EndermanEntity> {
    @Override
    public void update(Caster<?> source, EndermanEntity entity, Disguise spell) {
        if (source.getMaster().isSneaking() || source.getMaster().isSprinting()) {
            entity.setTarget(entity);
        } else {
            entity.setTarget(null);
        }

        ItemStack stack = source.getMaster().getStackInHand(Hand.MAIN_HAND);
        if (stack.getItem() instanceof BlockItem bi) {
            entity.setCarriedBlock(bi.getBlock().getDefaultState());
        } else {
            entity.setCarriedBlock(null);
        }
    }
}
