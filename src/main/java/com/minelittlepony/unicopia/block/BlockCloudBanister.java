package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCloudBanister extends BlockCloudFence {

    public static final double l = 11/16D;
    public static final double d = 5/16D;
    public static final double h = 2/16D;

    public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(d, 0, l, l, h, 1);
    public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0, 0, d, d, h, l);
    public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(d, 0, 0, l, h, d);
    public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(l, 0, d, 1, h, l);

    public static final AxisAlignedBB[] BOUNDING_BOXES = new AxisAlignedBB[] {
            new AxisAlignedBB(d, 0, d, l, h, l),
            new AxisAlignedBB(d, 0, d, l, h, 1),
            new AxisAlignedBB(0, 0, d, l, h, l),
            new AxisAlignedBB(0, 0, d, l, h, 1),
            new AxisAlignedBB(d, 0, 0, l, h, l),
            new AxisAlignedBB(d, 0, 0, l, h, 1),
            new AxisAlignedBB(0, 0, 0, l, h, l),
            new AxisAlignedBB(0, 0, 0, l, h, 1),
            new AxisAlignedBB(d, 0, d, 1, h, l),
            new AxisAlignedBB(d, 0, d, 1, h, 1),
            new AxisAlignedBB(0, 0, d, 1, h, l),
            new AxisAlignedBB(0, 0, d, 1, h, 1),
            new AxisAlignedBB(d, 0, 0, 1, h, l),
            new AxisAlignedBB(d, 0, 0, 1, h, 1),
            new AxisAlignedBB(0, 0, 0, 1, h, l),
            new AxisAlignedBB(0, 0, 0, 1, h, 1)
    };

    public BlockCloudBanister(String domain, String name) {
        super(domain, name);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Deprecated
    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
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

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = getActualState(state, source, pos);
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
