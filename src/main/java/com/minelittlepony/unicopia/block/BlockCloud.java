package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.SpeciesList;
import com.minelittlepony.unicopia.UBlocks;
import com.minelittlepony.unicopia.item.MossItem;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockCloud extends Block implements ICloudBlock, ITillable {

    private final CloudType variant;

    public BlockCloud(Material material, CloudType variant, String domain, String name) {
        super(FabricBlockSettings.of(material)
                .strength(0.5F, 1)
                .sounds(BlockSoundGroup.WOOL)
                .ticksRandomly()
                .build()
        );
        this.variant = variant;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return variant == CloudType.NORMAL;
    }

    @Override
    public boolean isOpaque(BlockState state) {
        return variant != CloudType.NORMAL;
    }

    @Override
    public void onScheduledTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(10) == 0) {
            pos = pos.offset(Direction.random(rand), 1 + rand.nextInt(2));
            state = world.getBlockState(pos);

            BlockState converted = MossItem.AFFECTED.getInverse().getConverted(state);

            if (!state.equals(converted)) {
                world.setBlockState(pos, converted);
            }
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return variant == CloudType.NORMAL ? BlockRenderLayer.TRANSLUCENT : super.getRenderLayer();
    }

    @Deprecated
    @Override
    public boolean isSideSolid(BlockState base_state, BlockView world, BlockPos pos, Direction side) {
        if (side == Direction.UP && (variant == CloudType.ENCHANTED || world.getBlockState(pos.up()).getBlock() instanceof ICloudBlock)) {
            return true;
        }

        return super.isSideSolid(base_state, world, pos, side);
    }

    @Override
    public boolean doesSideBlockRendering(BlockState state, BlockView world, BlockPos pos, Direction face) {

        BlockState beside = world.getBlockState(pos.offset(face));

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
    public void onEntityCollision(World w, BlockPos pos, BlockState state, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(w, pos, state, entity);
        }
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockView world, BlockPos pos, Entity entity) {
        return getCanInteract(state, entity) && super.canEntityDestroy(state, world, pos, entity);
    }

    @Deprecated
    public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, Box entityBox, List<Box> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
        if (getCanInteract(state, entity)) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, p_185477_7_);
        }
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, World worldIn, BlockPos pos) {
        if (CloudType.NORMAL.canInteract(player)) {
            return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
        }
        return -1;
    }

    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return variant;
    }

    @Deprecated
    @Override
    public RayTraceResult collisionRayTrace(BlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        if (!handleRayTraceSpecialCases(worldIn, pos, blockState)) {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
        return null;
    }

    @Override
    public boolean canBeTilled(ItemStack hoe, PlayerEntity player, World world, BlockState state, BlockPos pos) {
        return SpeciesList.instance().getPlayer(player).getSpecies().canInteractWithClouds()
                && ITillable.super.canBeTilled(hoe, player, world, state, pos);
    }

    @Override
    public BlockState getFarmlandState(ItemStack hoe, PlayerEntity player, World world, BlockState state, BlockPos pos) {
        return UBlocks.cloud_farmland.getDefaultState();
    }

}
