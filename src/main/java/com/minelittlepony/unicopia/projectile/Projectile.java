package com.minelittlepony.unicopia.projectile;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.SoundEmitter;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public interface Projectile extends ItemConvertible {
    static void makeDispensable(Projectile projectile) {
        DispenserBlock.registerBehavior(projectile.asItem(), new ProjectileDispenserBehavior(){
            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                ProjectileEntity p = projectile.createProjectile(stack, world, null);
                p.setPosition(position.getX(), position.getY(), position.getZ());
                return p;
            }

            @Override
            protected float getVariation() {
                return 0;
            }
        });
    }

    default TypedActionResult<ItemStack> triggerThrow(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        SoundEmitter.playSoundAt(player,
                getThrowSound(stack), SoundCategory.NEUTRAL,
                0.5F,
                0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (!world.isClient) {
            world.spawnEntity(createProjectile(stack.copyWithCount(1), world, player));
        }

        player.incrementStat(Stats.USED.getOrCreateStat(asItem()));

        if (!player.isCreative()) {
            stack.decrement(1);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    default ProjectileEntity createProjectile(ItemStack stack, World world, @Nullable PlayerEntity player) {
        MagicProjectileEntity projectile = player == null ? new MagicProjectileEntity(world) : new MagicProjectileEntity(world, player);
        projectile.setItem(stack);
        projectile.setThrowDamage(getProjectileDamage(stack));
        projectile.setMaxAge(-1);
        if (player != null) {
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0, 1.5F, 1);
        }
        return projectile;
    }

    SoundEvent getThrowSound(ItemStack stack);

    float getProjectileDamage(ItemStack stack);
}
