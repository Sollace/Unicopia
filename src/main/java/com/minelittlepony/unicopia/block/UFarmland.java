package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.util.PosHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public abstract class UFarmland extends BlockFarmland {

    public UFarmland(String domain, String name) {
        setTranslationKey(name);
        setRegistryName(domain, name);
        setHardness(0.6F);
        setSoundType(SoundType.GROUND);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        int i = state.getValue(MOISTURE);

        if (!hasWater(world, pos) && !world.isRainingAt(pos.up())) {
            if (i > 0) {
                world.setBlockState(pos, state.withProperty(MOISTURE, i - 1), 2);
            } else if (!hasCrops(world, pos)) {
                turnToDirt(world, pos, state);
            }
        } else if (i < 7) {
            world.setBlockState(pos, state.withProperty(MOISTURE, 7), 2);
        }
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
        if (shouldTrample(world, pos, entity, fallDistance)) {
            turnToDirt(world, pos, world.getBlockState(pos));
        }

        entity.fall(fallDistance, 1);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (shouldTurnToDirt(world, pos, state)) {
            turnToDirt(world, pos, state);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (shouldTurnToDirt(world, pos, state)) {
            turnToDirt(world, pos, state);
        }
    }

    public boolean hasCrops(World worldIn, BlockPos pos) {
        Block block = worldIn.getBlockState(pos.up()).getBlock();
        return block instanceof IPlantable
                && canSustainPlant(worldIn.getBlockState(pos), worldIn, pos, EnumFacing.UP, (IPlantable)block);
    }

    public boolean hasWater(World world, BlockPos pos) {
        return PosHelper.inRegion(pos.add(-4, 0, -4), pos.add(4, 1, 4)).anyMatch(p ->
            world.getBlockState(p).getMaterial() == Material.WATER
        );
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        return super.canSustainPlant(state, world, pos, direction, plantable)
                || plantable.getPlantType(world, pos.offset(direction)) == EnumPlantType.Crop;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        IBlockState dirtState = getDroppedState(state);

        return dirtState.getBlock().getItemDropped(dirtState, rand, fortune);
    }

    /**
     * Determines if this farmland should be trampled when an entity walks on it.
     */
    public boolean shouldTrample(World world, BlockPos pos, Entity entity, float fallDistance) {
        return !world.isRemote && entity.canTrample(world, this, pos, fallDistance);
    }

    /**
     * Determines if this farmland meets all the conditions for turning into dirt.
     */
    public boolean shouldTurnToDirt(World world, BlockPos pos, IBlockState state) {
        return world.getBlockState(pos.up()).getMaterial().isSolid();
    }

    /**
     * Turns this farmland into dirt or its dirt equivalent.
     */
    public void turnToDirt(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, getDirtState(world, pos, state));

        AxisAlignedBB bounds = getUpdateCollissionBounds(world, pos, state);

        if (bounds != null) {
            // Update entity positions so they don't fall through the block
            for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(null, bounds)) {

                double offset = Math.min(bounds.maxY - bounds.minY, bounds.maxY - entity.getEntityBoundingBox().minY);

                entity.setPositionAndUpdate(entity.posX, entity.posY + offset + 0.001D, entity.posZ);
            }
        }
    }

    protected AxisAlignedBB getUpdateCollissionBounds(World world, BlockPos pos, IBlockState state) {
        return field_194405_c.offset(pos);
    }

    protected IBlockState getDroppedState(IBlockState state) {
        return Blocks.DIRT.getDefaultState();
    }

    /**
     * Gets the state used to represent this block as a piece of dirt.
     */
    protected IBlockState getDirtState(World world, BlockPos pos, IBlockState state) {
        return getDroppedState(state);
    }
}
