package com.minelittlepony.unicopia.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.projectile.PhysicsBodyProjectileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class HeavyProjectileItem extends ProjectileItem {
    public HeavyProjectileItem(Item.Settings settings, float projectileDamage) {
        super(settings, projectileDamage);
    }

    @Override
    public PhysicsBodyProjectileEntity createProjectile(ItemStack stack, World world, @Nullable PlayerEntity player) {
        PhysicsBodyProjectileEntity projectile = player == null
                ? new PhysicsBodyProjectileEntity(world, stack.copyWithCount(1))
                : new PhysicsBodyProjectileEntity(world, player, stack.copyWithCount(1));
        if (player != null) {
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0, 1.5F, 1);
        }
        projectile.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
        return projectile;
    }

    @Override
    public SoundEvent getThrowSound(ItemStack stack) {
        return USounds.ENTITY_JAR_THROW;
    }
}
