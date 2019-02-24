package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.init.UBlocks;
import com.minelittlepony.unicopia.item.ItemMoss;
import com.minelittlepony.unicopia.player.PlayerSpeciesList;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCloud extends Block implements ICloudBlock, ITillable {

    private final CloudType variant;

    public BlockCloud(Material material, CloudType variant, String domain, String name) {
        super(material);
        setRegistryName(domain, name);
        setTranslationKey(name);

        setCreativeTab(CreativeTabs.MATERIALS);
        setHardness(0.5f);
        setResistance(1.0F);
        setSoundType(SoundType.CLOTH);
        setLightOpacity(20);
        setTickRandomly(true);

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
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (rand.nextInt(10) == 0) {
            pos = pos.offset(EnumFacing.random(rand), 1 + rand.nextInt(2));
            state = world.getBlockState(pos);

            IBlockState converted = ItemMoss.affected.getInverse().getConverted(state);

            if (!state.equals(converted)) {
                world.setBlockState(pos, converted);
            }
        }
    }

    @Override
    //Push player out of block
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
    public BlockRenderLayer getRenderLayer() {
        return variant == CloudType.NORMAL ? BlockRenderLayer.TRANSLUCENT : super.getRenderLayer();
    }

    @Deprecated
    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (side == EnumFacing.UP && (variant == CloudType.ENCHANTED || world.getBlockState(pos.up()).getBlock() instanceof ICloudBlock)) {
            return true;
        }

        return super.isSideSolid(base_state, world, pos, side);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {

        IBlockState beside = world.getBlockState(pos.offset(face));

        if (beside.getBlock() instanceof ICloudBlock) {
            ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

            if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                return true;
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
        return variant;
    }

    @Deprecated
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        if (!handleRayTraceSpecialCases(worldIn, pos, blockState)) {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
        return null;
    }

    @Override
    public boolean canBeTilled(ItemStack hoe, EntityPlayer player, World world, IBlockState state, BlockPos pos) {
        return PlayerSpeciesList.instance().getPlayer(player).getPlayerSpecies().canInteractWithClouds()
                && ITillable.super.canBeTilled(hoe, player, world, state, pos);
    }

    @Override
    public IBlockState getFarmlandState(ItemStack hoe, EntityPlayer player, World world, IBlockState state, BlockPos pos) {
        return UBlocks.cloud_farmland.getDefaultState();
    }

}
