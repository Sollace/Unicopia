package com.minelittlepony.unicopia.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.util.SoundEmitter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

abstract class ProjectileItem extends Item {

    private final float projectileDamage;

    public ProjectileItem(Settings settings, float projectileDamage) {
        super(settings);
        this.projectileDamage = projectileDamage;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

        if (isFood() && !player.isSneaking()) {
            return super.use(world, player, hand);
        }

        ItemStack stack = player.getStackInHand(hand);

        SoundEmitter.playSoundAt(player,
                getThrowSound(stack), SoundCategory.NEUTRAL,
                0.5F,
                0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (!world.isClient) {
            world.spawnEntity(createProjectile(stack, world, player));
        }

        player.incrementStat(Stats.USED.getOrCreateStat(this));

        if (!player.isCreative()) {
            stack.decrement(1);
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    protected ProjectileEntity createProjectile(ItemStack stack, World world, @Nullable PlayerEntity player) {
        MagicProjectileEntity projectile = player == null ? new MagicProjectileEntity(world) : new MagicProjectileEntity(world, player);
        projectile.setItem(stack);
        projectile.setThrowDamage(getProjectileDamage(stack));
        projectile.setMaxAge(-1);
        if (player != null) {
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0, 1.5F, 1);
        }
        return projectile;
    }

    protected abstract SoundEvent getThrowSound(ItemStack stack);

    protected float getProjectileDamage(ItemStack stack) {
        return projectileDamage;
    }

}
