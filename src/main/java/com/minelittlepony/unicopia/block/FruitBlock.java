package com.minelittlepony.unicopia.block;

import java.util.List;

import com.minelittlepony.unicopia.ability.EarthPonyKickAbility.Buckable;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public class FruitBlock extends Block implements Buckable {
    public static final int DEFAULT_FRUIT_SIZE = 5;
    public static final double DEFAULT_STEM_OFFSET = 2.6F;
    public static final VoxelShape DEFAULT_SHAPE = createFruitShape(DEFAULT_STEM_OFFSET, DEFAULT_FRUIT_SIZE);

    private final Direction attachmentFace;
    private final Block stem;
    private final VoxelShape shape;

    public static VoxelShape createFruitShape(double stemOffset, double fruitSize) {
        final double min = (16 - fruitSize) * 0.5;
        final double max = 16 - min;
        final double top = 16 - stemOffset;
        return createCuboidShape(min, top - fruitSize, min, max, top, max);
    }

    public FruitBlock(Settings settings, Direction attachmentFace, Block stem, VoxelShape shape) {
        this(settings.sounds(BlockSoundGroup.WOOD).pistonBehavior(PistonBehavior.DESTROY), attachmentFace, stem, shape, true);
    }

    public FruitBlock(Settings settings, Direction attachmentFace, Block stem, VoxelShape shape, boolean flammable) {
        super(settings.nonOpaque().suffocates(BlockConstructionUtils::never).blockVision(BlockConstructionUtils::never));
        this.attachmentFace = attachmentFace;
        this.stem = stem;
        this.shape = shape;

        if (flammable) {
            FlammableBlockRegistry.getDefaultInstance().add(this, 20, 50);
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos attachedPos = pos.offset(attachmentFace.getOpposite());
        BlockState attachedState = world.getBlockState(attachedPos);
        return canAttachTo(attachedState);
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    protected boolean canAttachTo(BlockState state) {
        return state.isOf(stem);
    }

    @Override
    public List<ItemStack> onBucked(ServerWorld world, BlockState state, BlockPos pos) {
        List<ItemStack> stacks = Block.getDroppedStacks(state, world, pos, null);
        world.breakBlock(pos, false);
        return stacks;
    }
}
