package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;

public interface ICloudBlock {

    CloudType getCloudMaterialType(IBlockState blockState);

    default boolean getCanInteract(IBlockState state, Entity e) {
        return getCloudMaterialType(state).canInteract(e);
    }

}
