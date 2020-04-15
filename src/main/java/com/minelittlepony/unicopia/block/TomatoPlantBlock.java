package com.minelittlepony.unicopia.block;

import java.util.List;

import com.minelittlepony.unicopia.gas.CloudFarmlandBlock;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Material;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.ViewableWorld;
import net.minecraft.world.World;
import net.minecraft.world.loot.context.LootContext;

public class TomatoPlantBlock extends CropBlock {

    public static final EnumProperty<Type> TYPE = EnumProperty.of("type", Type.class);

    public TomatoPlantBlock() {
        super(FabricBlockSettings.of(Material.PLANT)
                .noCollision()
                .strength(0.2F, 0.2F)
                .ticksRandomly()
                .lightLevel(1)
                .sounds(BlockSoundGroup.WOOD)
                .build()
        );
        setDefaultState(getDefaultState().with(TYPE, Type.NORMAL));

        // TODO: drops UItems.tomato_seeds x1
        // if mature: UItems.tomato
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
    public boolean canPlaceAt(BlockState state, ViewableWorld world, BlockPos pos) {
        if (world.getBlockState(pos.down()).getBlock() instanceof TomatoPlantBlock) {
            return true;
        }

        return super.canPlaceAt(state, world, pos);
    }

    @Override
    protected boolean canPlantOnTop(BlockState state, BlockView view, BlockPos pos) {
        return super.canPlantOnTop(state, view, pos)
                || state.getBlock() == UBlocks.cloud_farmland
                || state.getBlock() == UBlocks.tomato_plant
                || state.getBlock() == UBlocks.stick;
    }

    @Override
    @Deprecated
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder context) {
        List<ItemStack> drops = super.getDroppedStacks(state, context);

        drops.add(new ItemStack(Items.STICK, 1));

        if (isMature(state)) {
            drops.add(new ItemStack(state.get(TYPE).getCrop(), 1));
        } else {
            drops.add(new ItemStack(getSeedsItem()));
        }

        return drops;
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
        if (canPlantOnTop(state, world, pos)) {
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

        if (state.getBlock() instanceof CloudFarmlandBlock) {
            return getDefaultState().with(TYPE, Type.CLOUDSDALE);
        }

        if (state.getBlock() instanceof TomatoPlantBlock) {
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
