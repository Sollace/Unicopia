package com.minelittlepony.unicopia.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.entity.PhysicsBodyProjectileEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Position;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class HeavyProjectileItem extends ProjectileItem implements ProjectileDelegate.BlockHitListener {

    public HeavyProjectileItem(Settings settings, float projectileDamage) {
        super(settings, projectileDamage);
        DispenserBlock.registerBehavior(this, new ProjectileDispenserBehavior(){
            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                ProjectileEntity projectile = HeavyProjectileItem.this.createProjectile(stack, world, null);
                projectile.setPosition(position.getX(), position.getY(), position.getZ());
                return projectile;
            }
        });
    }

    @Override
    protected PhysicsBodyProjectileEntity createProjectile(ItemStack stack, World world, @Nullable PlayerEntity player) {
        PhysicsBodyProjectileEntity projectile = player == null ? new PhysicsBodyProjectileEntity(world) : new PhysicsBodyProjectileEntity(world, player);
        if (player != null) {
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0, 1.5F, 1);
        }
        projectile.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
        projectile.setStack(stack.copy().split(1));
        return projectile;
    }

    @Override
    protected SoundEvent getThrowSound(ItemStack stack) {
        return USounds.ENTITY_JAR_THROW;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile, BlockHitResult hit) {
        if (projectile.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
            float damage = projectile instanceof FlyingItemEntity ? getProjectileDamage(((FlyingItemEntity)projectile).getStack()) : 0;

            if (damage > 0 && projectile.world.random.nextInt(90) == 0) {
                if (projectile.world.getBlockState(hit.getBlockPos()).isIn(UTags.FRAGILE)) {
                    projectile.world.breakBlock(hit.getBlockPos(), true);
                }
            }
        }
    }
}
