package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.block.AbstractStairsBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class CloudStairsBlock<T extends Block & Gas> extends AbstractStairsBlock<T> implements Gas {

    public CloudStairsBlock(BlockState inherited, Settings settings) {
        super(inherited, settings);
    }

    @Override
    public CloudType getGasType(BlockState state) {
        return baseBlock.getGasType(baseBlockState);
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
        if (beside.getBlock() instanceof Gas) {
            Gas cloud = ((Gas)beside.getBlock());

            if (cloud.getGasType(beside) == getGasType(state)) {
                Direction front = state.get(FACING);
                BlockHalf half = state.get(HALF);

                VoxelShape shape = getOutlineShape(state, null, null, EntityContext.absent());

                boolean sideIsBack = Block.isFaceFullSquare(shape, face);
                boolean sideIsFront = Block.isFaceFullSquare(shape, face.getOpposite());
                boolean sideIsSide = !(sideIsBack || sideIsFront);

                if (beside.getBlock() == this) {
                    Direction bfront = beside.get(FACING);
                    BlockHalf bhalf = beside.get(HALF);

                    if (face == Direction.UP || face == Direction.DOWN) {
                        return half != bhalf
                                && ( (face == Direction.UP && half == BlockHalf.TOP)
                                  || (face == Direction.DOWN && half == BlockHalf.BOTTOM)
                            );
                    }

                    VoxelShape shapeBeside = getOutlineShape(beside, null, null, EntityContext.absent());

                    boolean bsideIsBack = Block.isFaceFullSquare(shapeBeside, face);
                    boolean bsideIsFront = Block.isFaceFullSquare(shapeBeside, face.getOpposite());
                    boolean bsideIsSide = !(bsideIsBack || bsideIsFront);

                    return sideIsBack
                            || (sideIsSide && bsideIsSide && front == bfront && half == bhalf);
                } else if (beside.getBlock() instanceof CloudSlabBlock) {
                    SlabType bhalf = beside.get(SlabBlock.TYPE);

                    if (face == Direction.UP || face == Direction.DOWN) {
                        return bhalf == SlabType.TOP && half == BlockHalf.BOTTOM;
                    }

                    return bhalf == SlabType.TOP && half == BlockHalf.BOTTOM;
                } else {
                    if (face == Direction.UP || face == Direction.DOWN) {
                        return half == BlockHalf.BOTTOM && face == Direction.DOWN;
                    }
                }

                return front == face;
            }
        }

        return false;
    }
}