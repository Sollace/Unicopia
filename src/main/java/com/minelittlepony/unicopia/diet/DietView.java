package com.minelittlepony.unicopia.diet;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public interface DietView {
    TypedActionResult<ItemStack> startUsing(ItemStack stack, World world, PlayerEntity user, Hand hand);

    void finishUsing(ItemStack stack, World world, LivingEntity entity);
}
