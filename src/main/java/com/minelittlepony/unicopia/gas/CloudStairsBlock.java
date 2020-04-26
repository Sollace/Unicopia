package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.block.AbstractStairsBlock;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;

public class CloudStairsBlock<T extends Block & Gas> extends AbstractStairsBlock<T> implements Gas {

    public CloudStairsBlock(BlockState inherited, Settings settings) {
        super(inherited, settings);
    }

    @Override
    public GasState getGasState(BlockState state) {
        return baseBlock.getGasState(baseBlockState);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        CloudInteractionContext ctx = (CloudInteractionContext)context;

        if (!getGasState(state).canPlace(ctx)) {
            return VoxelShapes.empty();
        }

        return super.getOutlineShape(state, view, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        CloudInteractionContext ctx = (CloudInteractionContext)context;

        if (!getGasState(state).canTouch(ctx)) {
            return VoxelShapes.empty();
        }

        return super.getCollisionShape(state, view, pos, context);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {
        if (beside.getBlock() instanceof Gas) {
            VoxelShape myShape = state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            VoxelShape otherShape = beside.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);

            return VoxelShapes.isSideCovered(myShape, otherShape, face);
        }

        return super.isSideInvisible(state, beside, face);
    }
}