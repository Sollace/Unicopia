package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.init.UItems;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.particle.Particles;

import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockGlowingGem extends BlockTorch implements ICloudBlock {

    public static PropertyBool ON = PropertyBool.create("on");

    private static final double A = 5/16D;
    private static final double B = 6/16D;

    private static final double C = 10/16D;

    // tiltedOffWall
    private static final double F = 10/16D;

    // tiltedMinY
    private static final double E = 3/16D;

    protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(
            7/16D, 0, 7/16D,
            9/16D, 1, 9/16D
    );
    protected static final AxisAlignedBB TORCH_NORTH_AABB = new AxisAlignedBB(
            B, E, F,
            C, 1, 1
    );
    protected static final AxisAlignedBB TORCH_SOUTH_AABB = new AxisAlignedBB(
            B, E, 0,
            C, 1, A
    );
    protected static final AxisAlignedBB TORCH_WEST_AABB = new AxisAlignedBB(
            F, E, B,
            1, 1, C
    );
    protected static final AxisAlignedBB TORCH_EAST_AABB = new AxisAlignedBB(
            0, E, B,
            A, 1, C
    );

    public BlockGlowingGem(String domain, String name) {
        super();
        setTranslationKey(name);
        setRegistryName(domain, name);

        setHardness(0);
        setLightLevel(1);
        setSoundType(SoundType.GLASS);
        setTickRandomly(true);

        setDefaultState(blockState.getBaseState()
                .withProperty(FACING, EnumFacing.UP)
                .withProperty(ON, true)
        );
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

        switch (state.getValue(FACING)) {
            case EAST: return TORCH_EAST_AABB;
            case WEST: return TORCH_WEST_AABB;
            case SOUTH: return TORCH_SOUTH_AABB;
            case NORTH: return TORCH_NORTH_AABB;
            default: return STANDING_AABB;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (!state.getValue(ON)) {
            ItemStack held = player.getHeldItem(hand);
            if (!held.isEmpty() && (held.getItem() == Items.FLINT_AND_STEEL || held.getItem() == Items.FIRE_CHARGE)) {

                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                world.setBlockState(pos, state.withProperty(ON, true));

                if (held.getItem() == Items.FLINT_AND_STEEL) {
                    held.damageItem(1, player);
                } else if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        EnumFacing facing = state.getValue(FACING);

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1;
        double z = pos.getZ() + 0.5;

        double drop = 0.22D;
        double variance = 0.27D;

        if (facing.getAxis().isHorizontal()) {
            facing = facing.getOpposite();

            x += variance * facing.getXOffset();
            y += drop;
            z += variance * facing.getZOffset();
        }

        if (state.getValue(ON)) {
            for (int i = 0; i < 3; i++) {
                Particles.instance().spawnParticle(UParticles.UNICORN_MAGIC, false,
                        x - 0.3, y - 0.3, z - 0.3,
                        rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            }
        } else {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRainingAt(pos)) {
            if (state.getValue(ON)) {
                world.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                world.setBlockState(pos, state.withProperty(ON, false));
            }
        } else {
            if (!state.getValue(ON)) {
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
                world.setBlockState(pos, state.withProperty(ON, true));
            }
        }
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(ON) && side == EnumFacing.DOWN ? state.getWeakPower(world, pos, side) : 0;
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return state.getValue(ON) && state.getValue(FACING) != side ? 12 : 0;
    }

    @Override
    public CloudType getCloudMaterialType(IBlockState blockState) {
        return CloudType.ENCHANTED;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        drops.add(new ItemStack(Items.STICK));

        Random rand = world instanceof World ? ((World)world).rand : RANDOM;

        if (rand.nextInt(10) == 0) {
            drops.add(new ItemStack(UItems.spell));
        } else {
            drops.add(new ItemStack(UItems.curse));
        }
    }

    @Override
    @Deprecated
    public int getLightValue(IBlockState state) {
        if (state.getValue(ON)) {
            return super.getLightValue(state);
        }

        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState();

        int facing = (meta % 5) + 1;

        return iblockstate
                .withProperty(FACING, EnumFacing.byIndex(facing))
                .withProperty(ON, meta > 5);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = Math.max(0, state.getValue(FACING).getIndex() - 1);
        if (state.getValue(ON)) {
            meta += EnumFacing.VALUES.length;
        }

        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ON);
    }
}
