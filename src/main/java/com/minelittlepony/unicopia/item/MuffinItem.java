package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.PhysicsBodyProjectileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MuffinItem extends HeavyProjectileItem {

    public MuffinItem(Settings settings, float projectileDamage) {
        super(settings, projectileDamage);
    }

    @Override
    protected PhysicsBodyProjectileEntity createProjectile(ItemStack stack, World world, PlayerEntity player) {
        PhysicsBodyProjectileEntity projectile = super.createProjectile(stack, world, player);
        projectile.setBouncy();
        projectile.setDamage(0);
        return projectile;
    }
}
