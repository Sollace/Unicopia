package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.util.PosHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class UFarmland extends FarmlandBlock {

    public UFarmland(String domain, String name) {
        setTranslationKey(name);
        setRegistryName(domain, name);
        setHardness(0.6F);
        setSoundType(SoundType.GROUND);
    }

    @Override
    public void updateTick(World world, BlockPos pos, BlockState state, Random rand) {
        int i = state.get(MOISTURE);

        if (!hasWater(world, pos) && !world.hasRain(pos.up())) {
            if (i > 0) {
                world.setBlockState(pos, state.with(MOISTURE, i - 1), 2);
            } else if (!hasCrops(world, pos)) {
                turnToDirt(world, pos, state);
            }
        } else if (i < 7) {
            world.setBlockState(pos, state.with(MOISTURE, 7), 2);
        }
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
        if (shouldTrample(world, pos, entity, fallDistance)) {
            turnToDirt(world, pos, world.getBlockState(pos));
        }

        entity.handleFallDamage(fallDistance, 1);
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (shouldTurnToDirt(world, pos, state)) {
            turnToDirt(world, pos, state);
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, BlockState state) {
        if (shouldTurnToDirt(world, pos, state)) {
            turnToDirt(world, pos, state);
        }
    }

    public boolean hasCrops(World worldIn, BlockPos pos) {
        Block block = worldIn.getBlockState(pos.up()).getBlock();
        return block instanceof Fertilizable
                && canSustainPlant(worldIn.getBlockState(pos), worldIn, pos, Direction.UP, (Fertilizable)block);
    }

    public boolean hasWater(World world, BlockPos pos) {
        return PosHelper.inRegion(pos.add(-4, 0, -4), pos.add(4, 1, 4)).anyMatch(p ->
            world.getBlockState(p).getMaterial() == Material.WATER
        );
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockView world, BlockPos pos, Direction direction, Fertilizable plantable) {
        return super.canSustainPlant(state, world, pos, direction, plantable)
                || plantable.getPlantType(world, pos.offset(direction)) == EnumPlantType.Crop;
    }

    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        BlockState dirtState = getDroppedState(state);

        return dirtState.getBlock().getItemDropped(dirtState, rand, fortune);
    }

    /**
     * Determines if this farmland should be trampled when an entity walks on it.
     */
    public boolean shouldTrample(World world, BlockPos pos, Entity entity, float fallDistance) {
        return !world.isClient && entity.canTrample(world, this, pos, fallDistance);
    }

    /**
     * Determines if this farmland meets all the conditions for turning into dirt.
     */
    public boolean shouldTurnToDirt(World world, BlockPos pos, BlockState state) {
        return world.getBlockState(pos.up()).getMaterial().isSolid();
    }

    /**
     * Turns this farmland into dirt or its dirt equivalent.
     */
    public void turnToDirt(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, getDirtState(world, pos, state));

        Box bounds = getUpdateCollissionBounds(world, pos, state);

        if (bounds != null) {
            // Update entity positions so they don't fall through the block
            for (Entity entity : world.getEntities(Entity.class, bounds)) {

                double offset = Math.min(bounds.maxY - bounds.minY, bounds.maxY - entity.getBoundingBox().minY);

                entity.setPosition(entity.x, entity.y + offset + 0.001D, entity.z);
            }
        }
    }

    protected Box getUpdateCollissionBounds(World world, BlockPos pos, BlockState state) {
        return field_194405_c.offset(pos);
    }

    protected BlockState getDroppedState(BlockState state) {
        return Blocks.DIRT.getDefaultState();
    }

    /**
     * Gets the state used to represent this block as a piece of dirt.
     */
    protected BlockState getDirtState(World world, BlockPos pos, BlockState state) {
        return getDroppedState(state);
    }
}
