package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.CloudType;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class CloudSlabBlock<T extends Block & ICloudBlock> extends USlab<T> implements ICloudBlock {

    public CloudSlabBlock(T modelBlock, Material material) {
        super(modelBlock, FabricBlockSettings.of(material).build());
    }

    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return modelBlock.getCloudMaterialType(blockState);
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {
        if (isDouble(state)) {
            if (beside.getBlock() instanceof ICloudBlock) {
                ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

                if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                    return true;
                }
            }

            return false;
        } else {
            if (beside.getBlock() instanceof ICloudBlock) {
                ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

                if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {

                    SlabType half = state.get(TYPE);

                    if (beside.getBlock() instanceof CloudStairsBlock) {
                        return beside.get(StairsBlock.HALF).ordinal() == state.get(TYPE).ordinal()
                           && beside.get(Properties.FACING) == face;
                    }

                    if (face == Direction.DOWN) {
                        return half == SlabType.BOTTOM;
                    }

                    if (face == Direction.UP) {
                        return half == SlabType.TOP;
                    }

                    if (beside.getBlock() == this) {
                        return beside.get(TYPE) == state.get(TYPE);
                    }
                }
            }
        }

        return false;
    }


}
