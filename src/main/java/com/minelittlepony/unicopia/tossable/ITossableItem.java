package com.minelittlepony.unicopia.tossable;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntityProjectile;
import com.minelittlepony.unicopia.item.IDispensable;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public interface ITossableItem extends ITossable<ItemStack>, IDispensable {

    default boolean canBeThrown(ItemStack stack) {
        return true;
    }

    @Override
    default ActionResult<ItemStack> dispenseStack(IBlockSource source, ItemStack stack) {

        if (canBeThrown(stack)) {
            stack = toss(source.getWorld(), BlockDispenser.getDispensePosition(source), (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING), stack);

            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Nullable
    default ITossed createProjectile(World world, EntityPlayer player) {
        return new EntityProjectile(world, player);
    }

    @Nullable
    default ITossed createProjectile(World world, IPosition pos) {
        return new EntityProjectile(world, pos.getX(), pos.getY(), pos.getZ());
    }

    default void toss(World world, ItemStack itemstack, EntityPlayer player) {

        world.playSound(null, player.posX, player.posY, player.posZ, getThrowSound(itemstack), SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            ITossed projectile = createProjectile(world, player);

            if (projectile == null) {
                return;
            }

            projectile.setItem(itemstack);
            projectile.setThrowDamage(getThrowDamage(itemstack));
            projectile.launch(player, player.rotationPitch, player.rotationYaw, 0, 1.5F, 1);

            world.spawnEntity((Entity)projectile);
        }

        if (!player.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        player.addStat(StatList.getObjectUseStats(itemstack.getItem()));
    }

    default ItemStack toss(World world, IPosition pos, EnumFacing facing, ItemStack stack) {
        ITossed projectile = createProjectile(world, pos);

        if (projectile == null) {
            return stack;
        }

        projectile.setItem(stack);
        projectile.setThrowDamage(getThrowDamage(stack));
        projectile.launch(facing.getXOffset(), facing.getYOffset() + 0.1F, facing.getZOffset(), 1.1F, 6);

        world.spawnEntity((Entity)projectile);

        stack.shrink(1);

        return stack;
    }
}
