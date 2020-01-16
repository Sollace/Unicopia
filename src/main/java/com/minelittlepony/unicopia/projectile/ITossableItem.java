package com.minelittlepony.unicopia.projectile;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.item.AdvancedProjectileEntity;
import com.minelittlepony.unicopia.magic.items.IDispensable;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public interface ITossableItem extends ITossable<ItemStack>, IDispensable {

    default boolean canBeThrown(ItemStack stack) {
        return true;
    }

    @Override
    default TypedActionResult<ItemStack> dispenseStack(BlockPointer source, ItemStack stack) {

        if (canBeThrown(stack)) {
            stack = toss(source.getWorld(),
                    DispenserBlock.getOutputLocation(source),
                    source.getBlockState().get(DispenserBlock.FACING),
                    stack);

            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        }

        return new TypedActionResult<>(ActionResult.PASS, stack);
    }

    @Nullable
    default IAdvancedProjectile createProjectile(World world, PlayerEntity player) {
        return new AdvancedProjectileEntity(null, world, player);
    }

    @Nullable
    default IAdvancedProjectile createProjectile(World world, Position pos) {
        return new AdvancedProjectileEntity(null, world, pos.getX(), pos.getY(), pos.getZ());
    }

    default void toss(World world, ItemStack itemstack, PlayerEntity player) {

        world.playSound(null, player.x, player.y, player.z, getThrowSound(itemstack), SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));

        if (!world.isClient) {
            IAdvancedProjectile projectile = createProjectile(world, player);

            if (projectile == null) {
                return;
            }

            projectile.setItem(itemstack);
            projectile.setThrowDamage(getThrowDamage(itemstack));
            projectile.launch(player, player.pitch, player.yaw, 0, 1.5F, 1);

            world.spawnEntity((Entity)projectile);
        }

        if (!player.abilities.creativeMode) {
            itemstack.decrement(1);
        }

        player.incrementStat(Stats.USED.getOrCreateStat(itemstack.getItem()));
    }

    default ItemStack toss(World world, Position pos, Direction facing, ItemStack stack) {
        IAdvancedProjectile projectile = createProjectile(world, pos);

        if (projectile == null) {
            return stack;
        }

        projectile.setItem(stack);
        projectile.setThrowDamage(getThrowDamage(stack));
        projectile.launch(facing.getOffsetX(), facing.getOffsetY() + 0.1F, facing.getOffsetZ(), 1.1F, 6);

        world.spawnEntity((Entity)projectile);

        stack.decrement(1);

        return stack;
    }
}
