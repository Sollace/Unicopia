package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.UItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateFactory;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockTomatoPlant extends CropBlock {

    public static final EnumProperty<Type> TYPE = EnumProperty.of("type", Type.class);

    public BlockTomatoPlant(String domain, String name) {
        setDefaultState(getDefaultState().with(TYPE, Type.NORMAL));
        setHardness(0.2F);
        setSoundType(SoundType.WOOD);
    }

    @Deprecated
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView source, BlockPos pos, EntityContext context) {
        Vec3d off = state.getOffsetPos(source, pos);
        return StickBlock.BOUNDING_BOX.offset(off.x, off.y, off.z);
    }

    @Override
    protected void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TYPE);
    }

    @Override
    public Block.OffsetType getOffsetType() {
        return Block.OffsetType.XZ;
    }

    @Override
    protected Item getSeedsItem() {
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
    public boolean canSustainPlant(BlockState state, BlockView world, BlockPos pos, Direction direction, Fertilizable plantable) {

        if (direction == Direction.UP && state.getBlock() instanceof BlockTomatoPlant) {
            return true;
        }

        return super.canSustainPlant(state, world, pos, direction, plantable);
    }

    @Override
    public void onScheduledTick(BlockState state, World world, BlockPos pos, Random rand) {
        checkAndDropBlock(world, pos, state);

        if (world.isBlockLoaded(pos) && world.getLightLevel(pos.up()) >= 9) {
            int i = getAge(state);

            if (i < getMaxAge()) {
                float f = getAvailableMoisture(this, world, pos);

                world.setBlockState(pos, state.with(getAgeProperty(), i + 1), 2);
            }
        }
    }

    @Override
    public int quantityDropped(BlockState state, int fortune, Random random) {
        return 1;
    }

    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {

        if (isMature(state)) {
            return state.get(TYPE).getCrop();
        }

        return getSeed();
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, WorldView world, BlockPos pos, BlockState state, int fortune) {
        Random rand = world instanceof World ? ((World)world).rand : RANDOM;

        drops.add(new ItemStack(Items.STICK, 1, 0));

        Item item = getItemDropped(state, rand, fortune);
        if (item != Items.AIR) {
            drops.add(new ItemStack(item, 1, damageDropped(state)));
        }
    }

    @Override
    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (hand == Hand.MAIN_HAND && isMature(state)) {
            if (player.getStackInHand(hand).isEmpty()) {
                Type type = state.get(TYPE);

                int good = getAge(state);
                int rotten = world.random.nextInt(good);

                good -= rotten;

                if (good > 0) {
                    dropStack(world, pos, new ItemStack(type.getCrop(), good));
                }
                if (rotten > 0) {
                    dropStack(world, pos, new ItemStack(type.getWaste(), rotten));
                }

                world.setBlockState(pos, state.with(getAgeProperty(), 0));

                return true;
            }
        }

        return false;
    }

    @Override
    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        int age = Math.min(getAge(state) + getGrowthAmount(world), getMaxAge());

        world.setBlockState(pos, state.with(getAgeProperty(), age), 2);
    }

    public boolean plant(World world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        if (block instanceof StickBlock && ((StickBlock)block).canSustainPlant(world, pos, this)) {

            world.setBlockState(pos, getPlacedState(world, pos, state).with(getAgeProperty(), 1));

            BlockSoundGroup sound = getSoundGroup(state);

            world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, sound.getVolume(), sound.getPitch() * 2);

            return true;
        }

        return false;
    }

    public BlockState getPlacedState(World world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof StickBlock) {
            pos = pos.down();
            return getPlacedState(world, pos, world.getBlockState(pos));
        }

        if (state.getBlock() instanceof BlockCloudFarm) {
            return getDefaultState().with(TYPE, Type.CLOUDSDALE);
        }

        if (state.getBlock() instanceof BlockTomatoPlant) {
            return getDefaultState().with(TYPE, state.get(TYPE));
        }

        return getDefaultState();
    }

    public enum Type implements StringIdentifiable {
        NORMAL,
        CLOUDSDALE;

        @Override
        public String toString() {
            return asString();
        }

        @Override
        public String asString() {
            return this == NORMAL ? "normal" : "cloudsdale";
        }

        public Item getCrop() {
            return this == CLOUDSDALE ? UItems.cloudsdale_tomato : UItems.tomato;
        }

        public Item getWaste() {
            return this == CLOUDSDALE ? UItems.rotten_cloudsdale_tomato : UItems.rotten_tomato;
        }
    }
}
