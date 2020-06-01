package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.block.SmartStairsBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class CloudStairsBlock extends SmartStairsBlock implements Gas {

    public CloudStairsBlock(BlockState inherited, Settings settings) {
        super(inherited, settings);
    }

    @Override
    public GasState getGasState(BlockState state) {
        return ((Gas)baseBlockState.getBlock()).getGasState(baseBlockState);
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