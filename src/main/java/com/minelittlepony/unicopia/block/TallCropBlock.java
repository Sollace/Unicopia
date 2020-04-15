package com.minelittlepony.unicopia.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;

public class TallCropBlock extends CropBlock {

    public static final IntProperty AGE = IntProperty.of("age", 0, 4);
    public static final EnumProperty<Half> HALF = EnumProperty.of("half", Half.class);

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
            Block.createCuboidShape(0, 0, 0, 16, 1.6, 16),
            Block.createCuboidShape(0, 0, 0, 16, 3.2, 16),
            Block.createCuboidShape(0, 0, 0, 16, 4.4, 16),
            Block.createCuboidShape(0, 0, 0, 16, 9.6, 16),
            Block.createCuboidShape(0, 0, 0, 16, 12.8, 16),
            Block.createCuboidShape(0, 0, 0, 16, 16,   16),
            Block.createCuboidShape(0, 0, 0, 16, 17.6, 16),
            Block.createCuboidShape(0, 0, 0, 16, 19.2, 16),
            Block.createCuboidShape(0, 0, 0, 16, 20.4, 16),
            Block.createCuboidShape(0, 0, 0, 16, 25.6, 16),
            Block.createCuboidShape(0, 0, 0, 16, 32,   16),
            Block.createCuboidShape(0, 0, 0, 16, 33.6, 16),
            Block.createCuboidShape(0, 0, 0, 16, 38.4, 16),
            Block.createCuboidShape(0, 0, 0, 16, 67.6, 16),
            Block.createCuboidShape(0, 0, 0, 16, 44.8, 16),
            Block.createCuboidShape(0, 0, 0, 16, 48,   16)
    };

    public TallCropBlock(Settings settings) {
        super(settings);
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
    public void onScheduledTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(10) != 0 && world.isBlockLoaded(pos) && world.getLightLevel(pos.up()) >= 9) {
            if (canGrow(world, rand, pos, state)) {
                growUpwards(world, pos, state, 1);
            }
        }
    }

    @Override
    protected boolean canPlantOnTop(BlockState state, BlockView view, BlockPos pos) {
        return state.getBlock() == this || super.canPlantOnTop(state, view, pos);
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
    public boolean canPlaceAt(BlockState state, ViewableWorld world, BlockPos pos) {
        return getHalf(state) != Half.BOTTOM || super.canPlaceAt(state, world, pos);
    }

    public void onPlayerDestroy(World worldIn, BlockPos pos, BlockState state) {
        breakConnectedBlocks(worldIn, pos, null);
    }

    @Override
    public void onBreak(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
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
    protected int getGrowthAmount(World world) {
        return super.getGrowthAmount(world) / 2;
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        if (getHalf(state) != Half.MIDDLE) {
            return VoxelShapes.empty();
        }
        return super.getCollisionShape(state, view, pos, context);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext ePos) {
        Vec3d offset = getOffsetPos(state, view, pos);
        return SHAPES[Math.min(SHAPES.length - 1, getFullAge(view, pos))].offset(offset.x, offset.y, offset.z);
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
        growUpwards(world, pos, state, getGrowthAmount(world));
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
            return super.toString().toLowerCase();
        }

        @Override
        public String asString() {
            return toString();
        }
    }
}
