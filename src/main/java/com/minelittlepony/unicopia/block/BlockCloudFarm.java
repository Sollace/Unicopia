package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.UBlocks;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockCloudFarm extends UFarmland implements ICloudBlock {

    public BlockCloudFarm(String domain, String name) {
        super(domain, name);

        setSoundType(SoundType.CLOTH);
    }

    @Override
    public boolean isAir(BlockState state, BlockView world, BlockPos pos) {
        return allowsFallingBlockToPass(state, world, pos);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {

        if (beside.getBlock() instanceof ICloudBlock) {
            ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

            if ((face.getAxis() == Axis.Y || cloud == this)) {
                if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                    return true;
                }
            }
        }

        return super.isSideInvisible(state, beside, face);
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!applyLanding(entityIn, fallDistance)) {
            super.onFallenUpon(world, pos, entityIn, fallDistance);
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
    public boolean canEntityDestroy(BlockState state, BlockView world, BlockPos pos, Entity entity) {
        return getCanInteract(state, entity) && super.canEntityDestroy(state, world, pos, entity);
    }

    @Deprecated
    @Override
    public RayTraceResult collisionRayTrace(BlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        if (!handleRayTraceSpecialCases(worldIn, pos, blockState)) {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
        return null;
    }

    @Deprecated
    public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, Box entityBox, List<Box> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
        if (getCanInteract(state, entity)) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, p_185477_7_);
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
    protected BlockState getDroppedState(BlockState state) {
        return UBlocks.normal_cloud.getDefaultState();
    }
}
