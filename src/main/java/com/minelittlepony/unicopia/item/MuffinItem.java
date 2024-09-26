package com.minelittlepony.unicopia.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MuffinItem extends HeavyProjectileItem {
    public MuffinItem(Item.Settings settings, float projectileDamage) {
        super(settings, projectileDamage);
    }

    @Override
    public PhysicsBodyProjectileEntity createProjectile(ItemStack stack, World world, @Nullable PlayerEntity player) {
        PhysicsBodyProjectileEntity projectile = super.createProjectile(stack, world, player);
        projectile.setBouncy();
        projectile.setDamage(0);
        UCriteria.THROW_MUFFIN.trigger(player);
        return projectile;
    }
}
