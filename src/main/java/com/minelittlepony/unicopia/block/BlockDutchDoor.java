package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDutchDoor extends UDoor {

    public BlockDutchDoor(Material material, String domain, String name, Supplier<Item> theItem) {
        super(material, domain, name, theItem);
    }

    @Override
    protected BlockPos getPrimaryDoorPos(IBlockState state, BlockPos pos) {
        return pos;
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return world.getBlockState(pos).getValue(OPEN);
    }

    @Override
    protected boolean onPowerStateChanged(World world, IBlockState state, BlockPos pos, boolean powered) {
        boolean result = super.onPowerStateChanged(world, state, pos, powered);

        IBlockState upper = world.getBlockState(pos.up());
        if (upper.getBlock() == this && upper.getValue(OPEN) != powered) {
            world.setBlockState(pos.up(), upper.withProperty(OPEN, powered));

            return true;
        }

        return result;
    }

    // UPPER - HALF/HINGE/POWER{/OPEN}
    // LOWER - HALF/FACING/FACING/OPEN

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {

        // copy properties in stored by the sibling block
        if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER) {
            IBlockState other = world.getBlockState(pos.up());

            if (other.getBlock() == this) {
                return state.withProperty(HINGE, other.getValue(HINGE))
                    .withProperty(POWERED, other.getValue(POWERED));
            }
        } else {
            IBlockState other = world.getBlockState(pos.down());

            if (other.getBlock() == this) {
                return state.withProperty(FACING, other.getValue(FACING));
            }
        }


        return state;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        boolean upper = (meta & 8) != 0;

        IBlockState state = getDefaultState()
                .withProperty(HALF, upper ? EnumDoorHalf.UPPER : EnumDoorHalf.LOWER)
                .withProperty(OPEN, (meta & 4) != 0);

        if (upper) {
            return state.withProperty(POWERED, (meta & 1) != 0)
                    .withProperty(HINGE, (meta & 2) != 0 ? EnumHingePosition.RIGHT : EnumHingePosition.LEFT);
        }

        return state.withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3).rotateYCCW());
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;

        if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
            i |= 8;

            if (state.getValue(POWERED)) {
                i |= 1;
            }

            if (state.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT) {
                i |= 2;
            }
        } else {
            i |= state.getValue(FACING).rotateY().getHorizontalIndex();
        }

        if (state.getValue(OPEN)) {
            i |= 4;
        }

        return i;
    }
}
