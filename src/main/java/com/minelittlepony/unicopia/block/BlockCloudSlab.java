package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.UBlocks;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCloudSlab extends BlockSlab implements ICloudBlock {

	public static final PropertyEnum<CloudType> VARIANT = PropertyEnum.create("variant", CloudType.class);

	private boolean isDouble;

	public BlockCloudSlab(boolean isDouble, Material material, String domain, String name) {
		super(material);

		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		setHardness(0.5F);
		setResistance(1.0F);
		setSoundType(SoundType.CLOTH);
		setLightOpacity(20);
		setTranslationKey(name);
		setRegistryName(domain, name);
		this.isDouble = isDouble;
		useNeighborBrightness = true;
	}

	@Override
    public boolean isTranslucent(IBlockState state) {
        return UBlocks.cloud.isTranslucent(state);
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
        return allowsFallingBlockToPass(state, world, pos);
    }

	@Override
    public boolean isOpaqueCube(IBlockState state) {
        return isDouble() ? UBlocks.cloud.isOpaqueCube(state) : false;
    }

	@Override
    public boolean isFullCube(IBlockState state) {
        return isDouble() ? UBlocks.cloud.isFullCube(state) : false;
    }

	@Override
    public boolean isNormalCube(IBlockState state) {
    	return isDouble() ? UBlocks.cloud.isNormalCube(state) : false;
    }

	@Override
    public BlockRenderLayer getRenderLayer() {
        return UBlocks.cloud.getRenderLayer();
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return super.isPassable(worldIn, pos);
    }

    @Override
    public void onFallenUpon(World w, BlockPos pos, Entity entity, float fallDistance) {
    	UBlocks.cloud.onFallenUpon(w, pos, entity, fallDistance);
    }

    @Override
    public void onLanded(World w, Entity entity) {
    	UBlocks.cloud.onLanded(w, entity);
    }

    @Override
    public void onEntityCollision(World w, BlockPos pos, IBlockState state, Entity entity) {
    	UBlocks.cloud.onEntityCollision(w, pos, state, entity);
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
    	if (getCanInteract(state, entity)) {
    		super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, p_185477_7_);
    	}
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World worldIn, BlockPos pos) {
        return UBlocks.cloud.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {

        IBlockState beside = world.getBlockState(pos.offset(face));

        if (beside.getBlock() instanceof ICloudBlock) {
            ICloudBlock cloud = ((ICloudBlock)beside.getBlock());

            if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                if (isDouble) {
                    return true;
                }

                if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                    return (state.getValue(HALF) == EnumBlockHalf.TOP) && (face == EnumFacing.UP);
                }

                if (beside.getBlock() == this) {
                    return beside.getValue(HALF) == state.getValue(HALF);
                } else {
                    if (beside.getBlock() instanceof BlockCloudStairs) {
                        return beside.getValue(BlockStairs.HALF).ordinal() == state.getValue(HALF).ordinal()
                           && beside.getValue(BlockStairs.FACING) == face;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return UBlocks.cloud.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(UBlocks.cloud_slab);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(UBlocks.cloud_slab), 2, getMetaFromState(state));
    }

    @Override
    public String getTranslationKey(int meta) {
        return super.getTranslationKey() + "." + CloudType.byMetadata(meta).getTranslationKey();
    }

    @Override
    public IProperty<CloudType> getVariantProperty() {
        return VARIANT;
    }

    public Object getVariant(ItemStack stack) {
        return CloudType.byMetadata(stack.getMetadata() & 7);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (CloudType i : CloudType.values()) {
            list.add(new ItemStack(this, 1, i.getMetadata()));
        }
    }


    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return (CloudType)blockState.getValue(VARIANT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState().withProperty(VARIANT, CloudType.byMetadata(meta & 7));
        if (!isDouble()) {
            state = state.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }
        return state;
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(IBlockState state) {
        byte mask = 0;
        int result = mask | getCloudMaterialType(state).getMetadata();
        if (!isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            result |= 8;
        }
        return result;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, isDouble() ?
                new IProperty[] {VARIANT}
              : new IProperty[] {HALF, VARIANT});
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getCloudMaterialType(state).getMetadata();
    }

    @Override
	public boolean isDouble() {
		return isDouble;
	}

	@Override
	public Comparable<CloudType> getTypeForItem(ItemStack stack) {
		return CloudType.byMetadata(stack.getMetadata() & 7);
	}
}
