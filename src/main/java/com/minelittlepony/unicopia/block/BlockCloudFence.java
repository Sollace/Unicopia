package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
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

public class BlockCloudFence extends BlockFence implements ICloudBlock {

    private final CloudType variant;

    public BlockCloudFence(Material material, CloudType variant, String domain, String name) {
        super(material, material.getMaterialMapColor());
        setTranslationKey(name);
        setRegistryName(domain, name);

        setHardness(0.5f);
        setResistance(1.0F);
        setLightOpacity(20);
        setSoundType(SoundType.CLOTH);
        useNeighborBrightness = true;

        this.variant = variant;
    }

    @Override
    public boolean isTranslucent(IBlockState state) {
        return variant == CloudType.NORMAL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return variant != CloudType.NORMAL;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return allowsFallingBlockToPass(state, world, pos);
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return variant;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return variant == CloudType.NORMAL ? BlockRenderLayer.TRANSLUCENT : super.getRenderLayer();
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

    public boolean canConnectTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        IBlockState myState = world.getBlockState(pos);

        return !(myState.getBlock() instanceof BlockCloudBanister)
                && super.canConnectTo(world, pos, facing);
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
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        if (getCanInteract(state, entity)) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, isActualState);
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

    @Deprecated
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        if (!handleRayTraceSpecialCases(world, pos, state)) {
            return super.collisionRayTrace(state, world, pos, start, end);
        }
        return null;
    }
}
