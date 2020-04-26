package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.block.AbstractSlabBlock;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class CloudSlabBlock<T extends Block & Gas> extends AbstractSlabBlock<T> implements Gas {

    public CloudSlabBlock(BlockState inherited, Material material) {
        super(inherited, FabricBlockSettings.of(material).build());
    }

    @Override
    public CloudType getGasType(BlockState blockState) {
        return modelBlock.getGasType(blockState);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        CloudInteractionContext ctx = (CloudInteractionContext)context;

        if (!ctx.canTouch(getGasType(state))) {
            return VoxelShapes.empty();
        }

        return super.getOutlineShape(state, view, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        CloudInteractionContext ctx = (CloudInteractionContext)context;

        if (!ctx.canTouch(getGasType(state))) {
            return VoxelShapes.empty();
        }

        return super.getCollisionShape(state, view, pos, context);
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {
        if (state.get(TYPE) == SlabType.DOUBLE) {
            if (beside.getBlock() instanceof Gas) {
                Gas cloud = ((Gas)beside.getBlock());

                if (cloud.getGasType(beside) == getGasType(state)) {
                    return true;
                }
            }

            return false;
        } else {
            if (beside.getBlock() instanceof Gas) {
                Gas cloud = ((Gas)beside.getBlock());

                if (cloud.getGasType(beside) == getGasType(state)) {

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
