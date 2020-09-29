package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;

import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class EndermanBehaviour extends EntityBehaviour<EndermanEntity> {
    @Override
    public void update(Caster<?> source, EndermanEntity entity, DisguiseSpell spell) {
        if (source.getOwner().isSneaking() || source.getOwner().isSprinting()) {
            entity.setTarget(entity);
        } else {
            entity.setTarget(null);
        }

        ItemStack stack = source.getOwner().getStackInHand(Hand.MAIN_HAND);
        if (stack.getItem() instanceof BlockItem) {
            entity.setCarriedBlock(((BlockItem)stack.getItem()).getBlock().getDefaultState());
        } else {
            entity.setCarriedBlock(null);
        }
    }
}
