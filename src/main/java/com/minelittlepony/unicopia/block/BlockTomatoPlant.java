package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.init.UItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;

public class BlockTomatoPlant extends BlockCrops {

    public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

    public BlockTomatoPlant(String domain, String name) {
        setRegistryName(domain, name);
        setTranslationKey(name);

        setDefaultState(getDefaultState().withProperty(TYPE, Type.NORMAL));
        setHardness(0.2F);
        setSoundType(SoundType.WOOD);
    }

    @Deprecated
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BlockStick.BOUNDING_BOX.offset(state.getOffset(source, pos));
    }

    @Deprecated
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getBoundingBox(world, pos);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE, AGE);
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    @Override
    protected Item getSeed() {
        return UItems.tomato_seeds;
    }

    @Override
    protected Item getCrop() {
        return UItems.tomato;
    }

    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        if (world.getBlockState(pos.down()).getBlock() instanceof BlockTomatoPlant) {
            return true;
        }

        return super.canPlaceBlockAt(world, pos);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {

        if (direction == EnumFacing.UP && state.getBlock() instanceof BlockTomatoPlant) {
            return true;
        }

        return super.canSustainPlant(state, world, pos, direction, plantable);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        checkAndDropBlock(world, pos, state);

        if (world.isAreaLoaded(pos, 1) && world.getLightFromNeighbors(pos.up()) >= 9) {
            int i = getAge(state);

            if (i < getMaxAge()) {
                float f = getGrowthChance(this, world, pos);

                if (ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt((int)(25 / f) + 1) == 0)) {
                    world.setBlockState(pos, state.withProperty(getAgeProperty(), i + 1), 2);

                    ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
                }
            }
        }
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return 1;
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {

        if (isMaxAge(state)) {
            return state.getValue(TYPE) == Type.CLOUDSDALE ? UItems.cloudsdale_tomato : UItems.tomato;
        }

        return getSeed();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World)world).rand : RANDOM;

        drops.add(new ItemStack(Items.STICK, 1, 0));

        Item item = getItemDropped(state, rand, fortune);
        if (item != Items.AIR) {
            drops.add(new ItemStack(item, 1, damageDropped(state)));
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (hand == EnumHand.MAIN_HAND && isMaxAge(state)) {
            if (player.getHeldItem(hand).isEmpty()) {
                Type type = state.getValue(TYPE);

                Item crop = type == Type.CLOUDSDALE ? UItems.cloudsdale_tomato : UItems.tomato;

                int good = getAge(state);
                int rotten = world.rand.nextInt(good);

                good -= rotten;

                if (good > 0) {
                    spawnAsEntity(world, pos, new ItemStack(crop, good, 0));
                }
                if (rotten > 0) {
                    spawnAsEntity(world, pos, new ItemStack(crop, rotten, 1));
                }

                world.setBlockState(pos, state.withProperty(getAgeProperty(), 0));

                return true;
            }
        }

        return false;
    }

    @Override
    public void grow(World worldIn, BlockPos pos, IBlockState state) {
        int age = Math.min(getAge(state) + getBonemealAgeIncrease(worldIn), getMaxAge());

        worldIn.setBlockState(pos, state.withProperty(getAgeProperty(), age), 2);
    }

    public boolean plant(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();

        if (block instanceof BlockStick && ((BlockStick)block).canSustainPlant(world, pos, this)) {

            world.setBlockState(pos, getPlacedState(world, pos, state).withProperty(getAgeProperty(), 1));

            SoundType sound = getSoundType(state, world, pos, null);

            world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, sound.getVolume(), sound.getPitch() * 2);

            return true;
        }

        return false;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int age = meta % (getMaxAge() + 1);
        int half = meta >> 3;;

        return withAge(age).withProperty(TYPE, Type.values()[half]);
    }

    public IBlockState getPlacedState(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() instanceof BlockStick) {
            pos = pos.down();
            return getPlacedState(world, pos, world.getBlockState(pos));
        }

        if (state.getBlock() instanceof BlockCloudFarm) {
            return getDefaultState().withProperty(TYPE, Type.CLOUDSDALE);
        }

        if (state.getBlock() instanceof BlockTomatoPlant) {
            return getDefaultState().withProperty(TYPE, state.getValue(TYPE));
        }

        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int age = getAge(state);
        int half = state.getValue(TYPE).ordinal();

        return (half << 3) + age;
    }

    public static enum Type implements IStringSerializable {
        NORMAL,
        CLOUDSDALE;

        public String toString() {
            return getName();
        }

        public String getName() {
            return this == NORMAL ? "normal" : "cloudsdale";
        }
    }
}
