package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCloudStairs extends BlockStairs implements ICloudBlock {

	protected Block theBlock;
	protected IBlockState theState;

	public BlockCloudStairs(IBlockState inherited, String domain, String name) {
		super(inherited);
		setTranslationKey(name);
		setRegistryName(domain, name);
		theBlock = inherited.getBlock();
		theState = inherited;
		useNeighborBrightness = true;

		fullBlock = isOpaqueCube(inherited);
	}

	@Override
	@Deprecated
	public boolean isTranslucent(IBlockState state) {
        return theBlock.isTranslucent(state);
    }

	@Override
	@Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

	@Override
	@Deprecated
	public boolean isNormalCube(IBlockState state) {
    	return theBlock.isNormalCube(state);
    }

	@Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return theBlock.isPassable(worldIn, pos);
    }

	@Override
    public void onFallenUpon(World w, BlockPos pos, Entity entity, float fallDistance) {
    	theBlock.onFallenUpon(w, pos, entity, fallDistance);
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return allowsFallingBlockToPass(state, world, pos);
    }

	@Override
    public void onLanded(World w, Entity entity) {
    	theBlock.onLanded(w, entity);
    }

	@Override
    public void onEntityCollision(World w, BlockPos pos, IBlockState state, Entity entity) {
		theBlock.onEntityCollision(w, pos, theState, entity);
	}

	@Override
	public void onEntityWalk(World w, BlockPos pos, Entity entity) {
		theBlock.onEntityWalk(w, pos, entity);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
        if (getCanInteract(theState, entity)) {
	        super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, p_185477_7_);
        }
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return theBlock.canEntityDestroy(state, world, pos, entity);
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        return theBlock.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        state = state.getActualState(world, pos);

        IBlockState beside = world.getBlockState(pos.offset(face)).getActualState(world, pos);

        if (beside.getBlock() instanceof ICloudBlock) {
            ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

            if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                EnumFacing front = state.getValue(FACING);
                EnumHalf half = state.getValue(HALF);

                boolean sideIsBack = state.getBlockFaceShape(world, pos, face) == BlockFaceShape.SOLID;
                boolean sideIsFront = state.getBlockFaceShape(world, pos, face.getOpposite()) == BlockFaceShape.SOLID;
                boolean sideIsSide = !(sideIsBack || sideIsFront);

                if (beside.getBlock() == this) {
                    EnumFacing bfront = beside.getValue(FACING);
                    EnumHalf bhalf = beside.getValue(HALF);

                    if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                        return half != bhalf
                                && ( (face == EnumFacing.UP && half == EnumHalf.TOP)
                                  || (face == EnumFacing.DOWN && half == EnumHalf.BOTTOM)
                            );
                    }

                    boolean bsideIsBack = beside.getBlockFaceShape(world, pos, face) == BlockFaceShape.SOLID;
                    boolean bsideIsFront = beside.getBlockFaceShape(world, pos, face.getOpposite()) == BlockFaceShape.SOLID;
                    boolean bsideIsSide = !(bsideIsBack || bsideIsFront);

                    return sideIsBack
                            || (sideIsSide && bsideIsSide && front == bfront && half == bhalf);
                } else if (beside.getBlock() instanceof BlockCloudSlab) {
                    EnumBlockHalf bhalf = beside.getValue(BlockSlab.HALF);

                    if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                        return bhalf == EnumBlockHalf.TOP && half == EnumHalf.BOTTOM;
                    }

                    return bhalf == EnumBlockHalf.TOP && half == EnumHalf.BOTTOM;
                } else {
                    if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                        return half == EnumHalf.BOTTOM && face == EnumFacing.DOWN;
                    }
                }

                return front == face;
            }
        }

        return false;
    }

    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return CloudType.NORMAL;
    }
}