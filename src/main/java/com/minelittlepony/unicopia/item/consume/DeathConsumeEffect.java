package com.minelittlepony.unicopia.item.consume;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.world.World;

public interface DeathConsumeEffect extends ConsumeEffect {
    @Override
    default boolean onConsume(World world, ItemStack stack, LivingEntity user) {
        return onConsume(world, stack, user, world.getDamageSources().magic());
    }

    boolean onConsume(World world, ItemStack stack, LivingEntity user, DamageSource damage);
}
