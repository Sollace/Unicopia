package com.minelittlepony.unicopia.world.block.gas;

import com.minelittlepony.unicopia.ducks.Farmland;
import com.minelittlepony.unicopia.world.block.UBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudFarmlandBlock extends FarmlandBlock implements Farmland, Gas {

    public CloudFarmlandBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {

        if (beside.getBlock() instanceof Gas) {
            Gas cloud = ((Gas)beside.getBlock());

            if (face.getAxis() == Axis.Y || cloud == this) {
                if (cloud.getGasState(beside) == getGasState(state)) {
                    return true;
                }
            }
        }

        return super.isSideInvisible(state, beside, face);
    }

    @Override
    public void onLandedUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!applyLanding(entityIn, fallDistance)) {
            super.onLandedUpon(world, pos, entityIn, fallDistance);
        }
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (!applyRebound(entity)) {
            super.onEntityLand(world, entity);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(state, w, pos, entity);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        CloudInteractionContext ctx = (CloudInteractionContext)context;

        if (!getGasState(state).canPlace(ctx)) {
            return VoxelShapes.empty();
        }

        return super.getOutlineShape(state, view, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        CloudInteractionContext ctx = (CloudInteractionContext)context;

        if (!getGasState(state).canTouch(ctx)) {
            return VoxelShapes.empty();
        }

        return super.getCollisionShape(state, view, pos, context);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (GasState.NORMAL.canTouch(player)) {
            return super.calcBlockBreakingDelta(state, player, world, pos);
        }
        return -1;
    }

    @Override
    public GasState getGasState(BlockState blockState) {
        return GasState.NORMAL;
    }

    @Override
    public BlockState getDirtState(BlockState state, World world, BlockPos pos) {
        return UBlocks.CLOUD_BLOCK.getDefaultState();
    }
}
