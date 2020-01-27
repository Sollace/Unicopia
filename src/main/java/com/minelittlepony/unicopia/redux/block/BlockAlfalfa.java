package com.minelittlepony.unicopia.redux.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.redux.item.UItems;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockAlfalfa extends CropBlock {

    public static final IntProperty AGE = IntProperty.of("age", 0, 4);
    public static final EnumProperty<Half> HALF = EnumProperty.of("half", Half.class);

    private static final Box[] BOUNDS = new Box[] {
        new Box(0, 0, 0, 1, 0.1, 1),
        new Box(0, 0, 0, 1, 0.2, 1),
        new Box(0, 0, 0, 1, 0.4, 1),
        new Box(0, 0, 0, 1, 0.6, 1),
        new Box(0, 0, 0, 1, 0.8, 1),
        new Box(0, 0, 0, 1, 1,   1),
        new Box(0, 0, 0, 1, 1.2, 1),
        new Box(0, 0, 0, 1, 1.4, 1),
        new Box(0, 0, 0, 1, 1.6, 1),
        new Box(0, 0, 0, 1, 1.8, 1),
        new Box(0, 0, 0, 1, 2,   1),
        new Box(0, 0, 0, 1, 2.2, 1),
        new Box(0, 0, 0, 1, 2.4, 1),
        new Box(0, 0, 0, 1, 2.6, 1),
        new Box(0, 0, 0, 1, 2.8, 1),
        new Box(0, 0, 0, 1, 3,   1)
    };

    public BlockAlfalfa() {
        setDefaultState(getDefaultState().with(HALF, Half.BOTTOM));
    }

    @Override
    public OffsetType getOffsetType() {
        return OffsetType.XZ;
    }

    @Override
    public IntProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 4;
    }

    @Override
    protected Item getSeedsItem() {
        return UItems.alfalfa_seeds;
    }

    @Override
    protected Item getCrop() {
        return UItems.alfalfa_seeds;
    }

    @Override
    public void onScheduledTick(BlockState state, World world, BlockPos pos, Random rand) {
        checkAndDropBlock(world, pos, state);
        if (rand.nextInt(10) != 0) {

            if (world.isBlockLoaded(pos) && world.getLightLevel(pos.up()) >= 9) {
                if (canGrow(world, rand, pos, state)) {
                    growUpwards(world, pos, state, 1);
                }
            }
        }
    }

    @Override
    protected boolean canSustainBush(BlockState state) {
        return super.canSustainBush(state) || state.getBlock() == this;
    }

    protected void growUpwards(World world, BlockPos pos, BlockState state, int increase) {
        boolean hasDown = world.getBlockState(pos.down()).getBlock() == this;
        boolean hasTrunk = world.getBlockState(pos.down(2)).getBlock() == this;
        boolean hasRoot = world.getBlockState(pos.down(3)).getBlock() == this;

        if (state.getBlock() != this) {
            if (state.isAir()) {
                if (!(hasDown && hasTrunk && hasRoot)) {
                    world.setBlockState(pos, withAge(increase).with(HALF, Half.TOP));
                }
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
            world.setBlockState(pos, withAge(age).with(HALF, Half.MIDDLE));
        } else if (hasUp) {
            world.setBlockState(pos, withAge(age).with(HALF, Half.BOTTOM));
        } else {
            world.setBlockState(pos, withAge(age).with(HALF, Half.TOP));
        }
    }

    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        if (state.get(HALF) != Half.BOTTOM) {
            return Items.AIR;
        }

        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockView world, BlockPos pos, BlockState state, int fortune) {
        Random rand = world instanceof World ? ((World)world).random : RANDOM;

        Item item = getItemDropped(state, rand, fortune);
        if (item != Items.AIR) {
            drops.add(new ItemStack(item, getFullAge(world, pos), damageDropped(state)));

            if (isMaxAge(state)) {
                drops.add(new ItemStack(UItems.alfalfa_leaves, rand.nextInt(10)));
            }
        }
    }

    @Override
    public int quantityDropped(BlockState state, int fortune, Random random) {
        return 1;
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, BlockState state) {
        return getHalf(state) != Half.BOTTOM || super.canBlockStay(world, pos, state);
    }

    public void onPlayerDestroy(World worldIn, BlockPos pos, BlockState state) {
        breakConnectedBlocks(worldIn, pos, null);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        breakConnectedBlocks(worldIn, pos, player);
    }

    protected void breakConnectedBlocks(World worldIn, BlockPos pos, @Nullable PlayerEntity player) {
        BlockState state = worldIn.getBlockState(pos);

        if (state.getBlock() != this) {
            return;
        }

        worldIn.breakBlock(pos, true);

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
    public boolean canCollideCheck(BlockState state, boolean hitIfLiquid) {
        return getHalf(state) != Half.MIDDLE;
    }

    @Override
    public Box getBoundingBox(BlockState state, BlockView source, BlockPos pos) {
        return BOUNDS[Math.min(BOUNDS.length - 1, getFullAge(source, pos))].offset(getOffset(state, source, pos));
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        Half half = getHalf(state);

        if (half == Half.MIDDLE || (half == Half.TOP && world.getBlockState(pos.down()).getBlock() == this)) {
            return false;
        }

        BlockState above = world.getBlockState(pos.up(1));
        BlockState higher = world.getBlockState(pos.up(2));

        boolean iCanGrow = !isMature(state);
        boolean aboveCanGrow = above.getBlock() != this || !isMature(above);
        boolean higherCanGrow = higher.getBlock() != this || !isMature(higher);

        return iCanGrow || aboveCanGrow || higherCanGrow;
    }

    @Override
    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        growUpwards(world, pos, state, getBonemealAgeIncrease(world));
    }

    protected BlockPos getTip(World world, BlockPos pos) {
        BlockPos above = pos.up();
        BlockState state = world.getBlockState(above);

        if (state.getBlock() == this) {
            return getTip(world, above);
        }

        return pos;
    }

    protected int getFullAge(BlockView world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        int age = 0;

        if (state.getBlock() == this) {
            age += state.get(getAgeProperty());
            age += getFullAge(world, pos.up());
        }

        return age;
    }

    protected Half getHalf(BlockState state) {
        return state.get(HALF);
    }

    public enum Half implements StringIdentifiable {
        TOP,
        MIDDLE,
        BOTTOM;

        boolean checkUp() {
            return this != TOP;
        }

        boolean checkDown() {
            return this != BOTTOM;
        }

        @Override
        public String toString() {
            return this == TOP ? "top" : this == MIDDLE ? "middle" : "bottom";
        }

        @Override
        public String asString() {
            return toString();
        }
    }
}
