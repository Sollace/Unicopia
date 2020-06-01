package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.block.SmartSlabBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class CloudSlabBlock extends SmartSlabBlock implements Gas {

    public CloudSlabBlock(BlockState inherited, Settings settings) {
        super(inherited, settings);
    }

    @Override
    public GasState getGasState(BlockState blockState) {
        return ((Gas)modelState.getBlock()).getGasState(blockState);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        if (getGasState(state).canPlace((CloudInteractionContext)context)) {
            return super.getOutlineShape(state, view, pos, context);
        }
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        if (getGasState(state).canTouch((CloudInteractionContext)context)) {
            return super.getCollisionShape(state, view, pos, context);
        }
        return VoxelShapes.empty();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {
        return isFaceCoverd(state, beside, face);
    }
}
