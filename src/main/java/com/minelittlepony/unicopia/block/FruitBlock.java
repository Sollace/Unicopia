package com.minelittlepony.unicopia.block;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.EarthPonyKickAbility.Buckable;

import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;

public class FruitBlock extends Block implements Buckable {
    public static final int DEFAULT_FRUIT_SIZE = 8;
    public static final VoxelShape DEFAULT_SHAPE = createFruitShape(DEFAULT_FRUIT_SIZE);

    public static final List<FruitBlock> REGISTRY = new ArrayList<>();

    private final Direction attachmentFace;
    private final Block stem;
    private final VoxelShape shape;

    public static VoxelShape createFruitShape(int fruitSize) {
        int min = (16 - fruitSize) / 2;
        int max = 16 - min;

        return VoxelShapes.cuboid(min / 16D, (max - fruitSize) / 16D, min / 16D, max / 16D, 1, max / 16D);
    }

    public FruitBlock(Settings settings, Direction attachmentFace, Block stem, VoxelShape shape) {
        super(settings.nonOpaque().suffocates(UBlocks::never).blockVision(UBlocks::never));
        this.attachmentFace = attachmentFace;
        this.stem = stem;
        this.shape = shape;
        REGISTRY.add(this);
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
