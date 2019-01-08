package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.UItems;

import net.minecraft.block.BlockCrops;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

public class BlockTomatoPlant extends BlockCrops {

    public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

    public BlockTomatoPlant(String domain, String name) {
        setRegistryName(domain, name);
        setTranslationKey(name);

        setDefaultState(getDefaultState().withProperty(TYPE, Type.NORMAL));
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

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (getAge(state) == 0) {
            return;
        }

        if (state.getValue(TYPE) != Type.CLOUDSDALE) {
            if (world.isAreaLoaded(pos, 1) && world.getBlockState(pos.down()).getBlock() instanceof BlockCloudFarm) {
                state = state.withProperty(TYPE, Type.CLOUDSDALE);

                world.setBlockState(pos, state);
            }
        }

        super.updateTick(world, pos, state, rand);
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return getAge(state) > 0 && super.canGrow(worldIn, pos, state, isClient);
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
        if (getAge(state) == 0) {
            return UItems.stick;
        }

        if (isMaxAge(state)) {
            return state.getValue(TYPE) == Type.CLOUDSDALE ? UItems.cloudsdale_tomato : UItems.tomato;
        }

        return getSeed();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World)world).rand : RANDOM;

        Item item = getItemDropped(state, rand, fortune);
        if (item != Items.AIR) {
            drops.add(new ItemStack(item, 1, damageDropped(state)));

            if (getAge(state) > 0) {
                drops.add(new ItemStack(item, rand.nextInt(2), 1));
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        if (hand == EnumHand.MAIN_HAND && isMaxAge(state)) {
            if (player.getHeldItem(hand).isEmpty()) {
                Item crop = state.getValue(TYPE) == Type.CLOUDSDALE ? UItems.cloudsdale_tomato : UItems.tomato;
                spawnAsEntity(world, pos, new ItemStack(crop, getAge(state), 0));
                world.setBlockState(pos, state.withProperty(getAgeProperty(), 0));

                return true;
            }
        }

        return false;
    }

    public boolean plant(World world, BlockPos pos, IBlockState state) {
        if (getAge(state) == 0) {
            world.setBlockState(pos, state.withProperty(getAgeProperty(), 1));
            return true;
        }

        return false;
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
