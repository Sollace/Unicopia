package com.minelittlepony.unicopia.block;

import java.util.List;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCloud extends Block implements ICloudBlock {

	public static final PropertyEnum<CloudType> VARIANT = PropertyEnum.create("variant", CloudType.class);

	public BlockCloud(Material material, String domain, String name) {
		super(material);
		setCreativeTab(CreativeTabs.MISC);
		setHardness(0.5f);
		setResistance(1.0F);
		setSoundType(SoundType.CLOTH);
		setRegistryName(domain, name);
		setLightOpacity(20);
		setTranslationKey(name);
		useNeighborBrightness = true;
	}

	@Override
    public boolean isTranslucent(IBlockState state) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    //Push player out of block
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
    	return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Deprecated
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
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

    //Can entities walk through?
    @Override
    public boolean isPassable(IBlockAccess w, BlockPos pos) {
        return super.isPassable(w, pos);
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        if (entityIn.isSneaking()) {
            super.onFallenUpon(world, pos, entityIn, fallDistance);
        } else {
            entityIn.fall(fallDistance, 0);
        }
    }

    @Override
    public void onLanded(World worldIn, Entity entity) {
        if (entity.isSneaking()) {
            super.onLanded(worldIn, entity);
        } else if (entity.motionY < 0) {
            if (Math.abs(entity.motionY) >= 0.25) {
                entity.motionY = -entity.motionY * 1.2;
            } else {
                entity.motionY = 0;
            }
        }
    }

    @Override
	public void onEntityCollision(World w, BlockPos pos, IBlockState state, Entity entity) {
        if (getCanInteract(state, entity)) {
    		if (!entity.isSneaking() && Math.abs(entity.motionY) >= 0.25) {
    			entity.motionY += 0.0155 * (entity.fallDistance < 1 ? 1 : entity.fallDistance);
    		} else {
    			entity.motionY = 0;
    		}

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
        if (!CloudType.NORMAL.canInteract(player)) {
            return -1;
        }
        return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }

	@Override
    public int damageDropped(IBlockState state) {
        return ((CloudType)state.getValue(VARIANT)).getMetadata();
    }

	@Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for (CloudType i : CloudType.values()) {
        	list.add(new ItemStack(this, 1, i.getMetadata()));
        }
    }

	@Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, CloudType.byMetadata(meta));
    }

	@Override
    public int getMetaFromState(IBlockState state) {
        return ((CloudType)state.getValue(VARIANT)).getMetadata();
    }

	@Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[] {VARIANT});
    }

    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return (CloudType)blockState.getValue(VARIANT);
    }

}
