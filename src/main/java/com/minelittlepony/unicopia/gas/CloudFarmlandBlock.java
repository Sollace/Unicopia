package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.ducks.Farmland;

import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
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
                if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
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

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (CloudType.NORMAL.canInteract(player)) {
            return super.calcBlockBreakingDelta(state, player, world, pos);
        }
        return -1;
    }

    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return CloudType.NORMAL;
    }

    @Override
    public BlockState getDirtState(BlockState state, World world, BlockPos pos) {
        return UBlocks.CLOUD_BLOCK.getDefaultState();
    }
}
