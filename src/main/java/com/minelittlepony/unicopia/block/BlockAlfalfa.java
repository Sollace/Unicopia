package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.UItems;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;

public class BlockAlfalfa extends BlockCrops {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 4);
    public static final PropertyEnum<Half> HALF = PropertyEnum.create("half", Half.class);

    private static final AxisAlignedBB[] BOUNDS = new AxisAlignedBB[] {
        new AxisAlignedBB(0, 0, 0, 1, 0.1, 1),
        new AxisAlignedBB(0, 0, 0, 1, 0.2, 1),
        new AxisAlignedBB(0, 0, 0, 1, 0.4, 1),
        new AxisAlignedBB(0, 0, 0, 1, 0.6, 1),
        new AxisAlignedBB(0, 0, 0, 1, 0.8, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1,   1),
        new AxisAlignedBB(0, 0, 0, 1, 1.2, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1.4, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1.6, 1),
        new AxisAlignedBB(0, 0, 0, 1, 1.8, 1),
        new AxisAlignedBB(0, 0, 0, 1, 2,   1),
        new AxisAlignedBB(0, 0, 0, 1, 2.2, 1),
        new AxisAlignedBB(0, 0, 0, 1, 2.4, 1),
        new AxisAlignedBB(0, 0, 0, 1, 2.6, 1),
        new AxisAlignedBB(0, 0, 0, 1, 2.8, 1),
        new AxisAlignedBB(0, 0, 0, 1, 3,   1)
    };

    public BlockAlfalfa(String domain, String name) {
        setRegistryName(domain, name);
        setTranslationKey(name);

        setDefaultState(getDefaultState().withProperty(HALF, Half.BOTTOM));
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
    }

    @Override
    protected PropertyInteger getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 4;
    }

    @Override
    protected Item getSeed() {
        return UItems.alfalfa_seeds;
    }

    @Override
    protected Item getCrop() {
        return UItems.alfalfa_seeds;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        checkAndDropBlock(world, pos, state);
        if (rand.nextInt(10) != 0) {

            if (world.isAreaLoaded(pos, 1) && world.getLightFromNeighbors(pos.up()) >= 9) {
                if (canGrow(world, pos, state, false)) {
                    float f = getGrowthChance(this, world, pos);

                    if(ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt((int)(25 / f) + 1) == 0)) {
                        growUpwards(world, pos, state, 1);
                        ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
                    }
                }
            }
        }
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return super.canSustainBush(state) || state.getBlock() == this;
    }

    protected void growUpwards(World world, BlockPos pos, IBlockState state, int increase) {
        boolean hasDown = world.getBlockState(pos.down()).getBlock() == this;
        boolean hasTrunk = world.getBlockState(pos.down(2)).getBlock() == this;
        boolean hasRoot = world.getBlockState(pos.down(3)).getBlock() == this;

        if (state.getBlock().isAir(state, world, pos)) {
            if (!(hasDown && hasTrunk && hasRoot)) {
                world.setBlockState(pos, withAge(increase).withProperty(HALF, Half.TOP));
            }
            return;
        }

        int age = getAge(state) + increase;
        int max = getMaxAge();

        if (age > max) {
            if (!(hasDown && hasTrunk)) {
                growUpwards(world, pos.up(), world.getBlockState(pos.up()), age - max);
            }
            age = max;
        }

        boolean hasUp = world.getBlockState(pos.up()).getBlock() == this;

        if (hasDown && hasUp) {
            world.setBlockState(pos, withAge(age).withProperty(HALF, Half.MIDDLE));
        } else if (hasUp) {
            world.setBlockState(pos, withAge(age).withProperty(HALF, Half.BOTTOM));
        } else {
            world.setBlockState(pos, withAge(age).withProperty(HALF, Half.TOP));
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (state.getValue(HALF) != Half.BOTTOM) {
            return Items.AIR;
        }

        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World)world).rand : RANDOM;

        Item item = getItemDropped(state, rand, fortune);
        if (item != Items.AIR) {
            drops.add(new ItemStack(item, getFullAge(world, pos), damageDropped(state)));

            if (isMaxAge(state)) {
                drops.add(new ItemStack(UItems.alfalfa_leaves, rand.nextInt(10)));
            }
        }
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return 1;
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return getHalf(state) != Half.BOTTOM || super.canBlockStay(world, pos, state);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        breakConnectedBlocks(worldIn, pos, player);
    }

    protected void breakConnectedBlocks(World worldIn, BlockPos pos, EntityPlayer player) {
        IBlockState state = worldIn.getBlockState(pos);

        if (state.getBlock() != this) {
            return;
        }

        if (player.capabilities.isCreativeMode) {
            worldIn.setBlockToAir(pos);
        } else {
            if (worldIn.isRemote) {
                worldIn.setBlockToAir(pos);
            } else {
                worldIn.destroyBlock(pos, true);
            }
        }

        Half half = getHalf(state);

        if (half.checkDown()) {
            breakConnectedBlocks(worldIn, pos.down(), player);
        }
        if (half.checkUp()) {
            breakConnectedBlocks(worldIn, pos.up(), player);
        }
    }

    @Override
    protected int getBonemealAgeIncrease(World world) {
        return super.getBonemealAgeIncrease(world) / 2;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HALF, AGE);
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return getHalf(state) != Half.MIDDLE;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDS[Math.min(BOUNDS.length - 1, getFullAge(source, pos))];
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        Half half = getHalf(state);

        if (half == Half.MIDDLE || (half == Half.TOP && world.getBlockState(pos.down()).getBlock() == this)) {
            return false;
        }

        IBlockState above = world.getBlockState(pos.up(1));
        IBlockState higher = world.getBlockState(pos.up(2));

        boolean iCanGrow = !isMaxAge(state);
        boolean aboveCanGrow = above.getBlock() != this || !isMaxAge(above);
        boolean higherCanGrow = higher.getBlock() != this || !isMaxAge(higher);

        return iCanGrow || aboveCanGrow || higherCanGrow;
    }

    @Override
    public void grow(World world, BlockPos pos, IBlockState state) {
        growUpwards(world, pos, state, getBonemealAgeIncrease(world));
    }

    protected BlockPos getTip(World world, BlockPos pos) {
        BlockPos above = pos.up();
        IBlockState state = world.getBlockState(above);

        if (state.getBlock() == this) {
            return getTip(world, above);
        }

        return pos;
    }

    protected int getFullAge(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        int age = 0;

        if (state.getBlock() == this) {
            age += state.getValue(getAgeProperty());

            age += getFullAge(world, pos.up());
        }

        return age;
    }

    protected Half getHalf(IBlockState state) {
        return (Half)state.getValue(HALF);
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int age = meta % (getMaxAge() + 1);
        int half = (int)Math.floor(meta / (getMaxAge() + 1)) % Half.values().length;

        return withAge(age).withProperty(HALF, Half.values()[half]);
    }

    // 0: age:0, half:0
    // 1: age:1, half:0
    // 2: age:2, half:0
    // 3: age:3, half:0
    // 4: age:4, half:0
    // 5: age:0, half:1
    // 6: age:1, half:1
    // 7: age:2, half:1
    // 8: age:3, half:1
    // 9: age:4, half:1
    //10: age:0, half:2
    //11: age:1, half:2
    //12: age:2, half:2
    //13: age:3, half:2
    //14: age:4, half:2

    @Override
    public int getMetaFromState(IBlockState state) {
        int age = getAge(state);
        int half = getHalf(state).ordinal();

        return (half * (getMaxAge() + 1)) + age;
    }

    public static enum Half implements IStringSerializable {
        TOP,
        MIDDLE,
        BOTTOM;

        boolean checkUp() {
            return this != TOP;
        }

        boolean checkDown() {
            return this != BOTTOM;
        }

        public String toString() {
            return getName();
        }

        public String getName() {
            return this == TOP ? "top" : this == MIDDLE ? "middle" : "bottom";
        }
    }
}
