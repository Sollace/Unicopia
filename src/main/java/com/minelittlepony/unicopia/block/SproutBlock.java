package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.*;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public class SproutBlock extends CropBlock implements TintedBlock {
    private static final VoxelShape[] AGE_TO_SHAPE = new VoxelShape[]{
            Block.createCuboidShape(7, 0, 7, 9, 2, 9),
            Block.createCuboidShape(7, 0, 7, 9, 4, 9),
            Block.createCuboidShape(7, 0, 7, 9, 6, 9),
            Block.createCuboidShape(7, 0, 7, 9, 8, 9),
            Block.createCuboidShape(7, 0, 7, 9, 10, 9),
            Block.createCuboidShape(7, 0, 7, 9, 12, 9),
            Block.createCuboidShape(7, 0, 7, 9, 14, 9),
            Block.createCuboidShape(7, 0, 7, 9, 16, 9)
    };

    private final ItemConvertible seeds;

    private final Supplier<BlockState> matureState;

    private final int overlay;

    public SproutBlock(int overlay, ItemConvertible seeds, Supplier<BlockState> matureState) {
        super(Settings.of(Material.PLANT).noCollision().ticksRandomly().breakInstantly().sounds(BlockSoundGroup.STEM));
        this.seeds = seeds;
        this.matureState = matureState;
        this.overlay = overlay;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return AGE_TO_SHAPE[state.get(getAgeProperty())];
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        onGrow(world, world.getBlockState(pos), pos);
    }

    @Override
    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        super.applyGrowth(world, pos, state);
        onGrow(world, world.getBlockState(pos), pos);
    }

    @Override
    protected ItemConvertible getSeedsItem() {
        return seeds;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return new ItemStack(seeds.asItem());
    }

    protected void onGrow(World world, BlockState state, BlockPos pos) {
        if (isMature(state)) {
            mature(world, state, pos);
        }
    }

    protected void mature(World world, BlockState state, BlockPos pos) {
        state = matureState.get();
        world.setBlockState(pos, matureState.get());
        BlockSoundGroup group = state.getSoundGroup();
        world.playSound(null, pos, group.getPlaceSound(), SoundCategory.BLOCKS, group.getVolume(), group.getPitch());
    }

    @Override
    public int getTint(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int foliageColor) {
        return TintedBlock.blend(foliageColor, overlay);
    }
}
