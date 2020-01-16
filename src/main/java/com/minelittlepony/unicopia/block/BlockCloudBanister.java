package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCloudBanister extends BlockCloudFence {

    public static final double l = 11/16D;
    public static final double d = 5/16D;
    public static final double h = 2/16D;

    public static final Box SOUTH_AABB = new Box(d, 0, l, l, h, 1);
    public static final Box WEST_AABB = new Box(0, 0, d, d, h, l);
    public static final Box NORTH_AABB = new Box(d, 0, 0, l, h, d);
    public static final Box EAST_AABB = new Box(l, 0, d, 1, h, l);

    public static final Box[] BOUNDING_BOXES = new Box[] {
            new Box(d, 0, d, l, h, l),
            new Box(d, 0, d, l, h, 1),
            new Box(0, 0, d, l, h, l),
            new Box(0, 0, d, l, h, 1),
            new Box(d, 0, 0, l, h, l),
            new Box(d, 0, 0, l, h, 1),
            new Box(0, 0, 0, l, h, l),
            new Box(0, 0, 0, l, h, 1),
            new Box(d, 0, d, 1, h, l),
            new Box(d, 0, d, 1, h, 1),
            new Box(0, 0, d, 1, h, l),
            new Box(0, 0, d, 1, h, 1),
            new Box(d, 0, 0, 1, h, l),
            new Box(d, 0, 0, 1, h, 1),
            new Box(0, 0, 0, 1, h, l),
            new Box(0, 0, 0, 1, h, 1)
    };

    public BlockCloudBanister(Material material, String domain, String name) {
        super(material, CloudType.ENCHANTED, domain, name);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canConnectTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        IBlockState myState = world.getBlockState(pos);

        return myState.getBlock() instanceof BlockCloudBanister
                && super.canConnectTo(world, pos, facing);
    }

    @Override
    public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        IBlockState state = world.getBlockState(pos.offset(facing));

        return state.getBlock() instanceof BlockCloudBanister
                && state.getMaterial() == world.getBlockState(pos).getMaterial();
    }

    @Deprecated
    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, Box entityBox, List<Box> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
        if (!getCanInteract(state, entity)) {
            return;
        }

        if (!isActualState) {
            state = state.getActualState(world, pos);
        }

        if (state.getValue(NORTH)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_AABB);
        }

        if (state.getValue(EAST)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_AABB);
        }

        if (state.getValue(SOUTH)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_AABB);
        }

        if (state.getValue(WEST)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_AABB);
        }
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public Box getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = state.getActualState(source, pos);
        return BOUNDING_BOXES[getBoundingBoxIdx(state)];
    }

    public static int getBoundingBoxIdx(IBlockState state) {
        int i = 0;

        if (state.getValue(NORTH)) {
            i |= 1 << EnumFacing.NORTH.getHorizontalIndex();
        }

        if (state.getValue(EAST)) {
            i |= 1 << EnumFacing.EAST.getHorizontalIndex();
        }

        if (state.getValue(SOUTH)) {
            i |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
        }

        if (state.getValue(WEST)) {
            i |= 1 << EnumFacing.WEST.getHorizontalIndex();
        }

        return i;
    }
}
