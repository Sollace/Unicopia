package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.init.UBlocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCloudFarm extends UFarmland implements ICloudBlock {

    public BlockCloudFarm(String domain, String name) {
        super(domain, name);

        setSoundType(SoundType.CLOTH);
    }

    @Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return allowsFallingBlockToPass(state, world, pos);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {

        IBlockState beside = world.getBlockState(pos.offset(face));

        if (beside.getBlock() instanceof ICloudBlock) {
            ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

            if ((face == EnumFacing.DOWN || face == EnumFacing.UP || cloud == this)) {
                if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                    return true;
                }
            }
        }

        return super.doesSideBlockRendering(state, world, pos, face);
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!applyLanding(entityIn, fallDistance)) {
            super.onFallenUpon(world, pos, entityIn, fallDistance);
        }
    }

    @Override
    public void onLanded(World worldIn, Entity entity) {
        if (!applyRebound(entity)) {
            super.onLanded(worldIn, entity);
        }
    }

    @Override
    public void onEntityCollision(World w, BlockPos pos, IBlockState state, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(w, pos, state, entity);
        }
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return getCanInteract(state, entity) && super.canEntityDestroy(state, world, pos, entity);
    }

    @Deprecated
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        if (!handleRayTraceSpecialCases(worldIn, pos, blockState)) {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
        return null;
    }

    @Deprecated
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
        if (getCanInteract(state, entity)) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, p_185477_7_);
        }
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        if (CloudType.NORMAL.canInteract(player)) {
            return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
        }
        return -1;
    }


    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return CloudType.NORMAL;
    }

    @Override
    protected IBlockState getDroppedState(IBlockState state) {
        return UBlocks.cloud.getDefaultState();
    }
}
