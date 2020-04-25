package com.minelittlepony.unicopia.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TomatoItem extends Item {

    public TomatoItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entity) {
        stack = super.finishUsing(stack, world, entity);
        entity.removeStatusEffect(StatusEffects.NAUSEA);
        return stack;
    }

}
