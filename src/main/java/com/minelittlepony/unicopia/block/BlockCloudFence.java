package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockCloudFence extends FenceBlock implements ICloudBlock {

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
    public boolean isTranslucent(BlockState state) {
        return variant == CloudType.NORMAL;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isAir(BlockState state, BlockView world, BlockPos pos) {
        return allowsFallingBlockToPass(state, world, pos);
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return variant;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
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

    public boolean canConnectTo(BlockView world, BlockPos pos, Direction facing) {
        BlockState myState = world.getBlockState(pos);

        return !(myState.getBlock() instanceof BlockCloudBanister)
                && super.canConnectTo(world, pos, facing);
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
    public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, Box entityBox, List<Box> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        if (getCanInteract(state, entity)) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, isActualState);
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

    @Deprecated
    @Override
    public RayTraceResult collisionRayTrace(BlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        if (!handleRayTraceSpecialCases(world, pos, state)) {
            return super.collisionRayTrace(state, world, pos, start, end);
        }
        return null;
    }
}
