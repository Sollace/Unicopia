package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ICloudBlock {

    CloudType getCloudMaterialType(IBlockState blockState);

    default boolean handleRayTraceSpecialCases(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            EntityPlayer player = Minecraft.getMinecraft().player;

            if (!getCanInteract(state, player)) {
                return true;
            }

            ItemStack main = player.getHeldItemMainhand();
            if (main.isEmpty()) {
                main = player.getHeldItemOffhand();
            }

            if (!main.isEmpty() && main.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)main.getItem()).getBlock();

                if (block == null) {
                    return false;
                }

                if (block instanceof BlockTorch && getCloudMaterialType(state) != CloudType.NORMAL) {
                    return false;
                }

                if (block != Blocks.AIR && !(block instanceof ICloudBlock)) {
                    return true;
                }
            }
        }

        return false;
    }

    default boolean getCanInteract(IBlockState state, Entity e) {
        if (getCloudMaterialType(state).canInteract(e)) {
            if (e instanceof EntityItem) {
                e.setNoGravity(true);
            }
            return true;
        }

        return false;
    }

    default boolean isDense(IBlockState blockState) {
        return getCloudMaterialType(blockState) != CloudType.NORMAL;
    }

    default boolean allowsFallingBlockToPass(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (isDense(state)) {
            return false;
        }

        Block above = world.getBlockState(pos.up()).getBlock();
        return !(above instanceof ICloudBlock) && above instanceof BlockFalling;
    }
}
