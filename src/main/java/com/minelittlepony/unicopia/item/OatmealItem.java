package com.minelittlepony.unicopia.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class OatmealItem extends StewItem {

    public OatmealItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            user.clearStatusEffects();
        }
        return super.finishUsing(stack, world, user);
    }
}
