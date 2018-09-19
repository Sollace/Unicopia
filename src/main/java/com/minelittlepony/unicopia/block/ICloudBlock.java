package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface ICloudBlock {

    CloudType getCloudMaterialType(IBlockState blockState);

    default boolean getCanInteract(IBlockState state, Entity e) {
        return getCloudMaterialType(state).canInteract(e);
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
