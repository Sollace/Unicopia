package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudStairsBlock extends StairsBlock implements Soakable {

    private final CloudBlock baseBlock;
    private final @Nullable Supplier<Soakable> soggyBlock;

    public CloudStairsBlock(BlockState baseState, Settings settings, @Nullable Supplier<Soakable> soggyBlock) {
        super(baseState, settings);
        this.baseBlock = (CloudBlock)baseState.getBlock();
        this.soggyBlock = soggyBlock;
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        baseBlock.onEntityLand(world, entity);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        baseBlock.onLandedUpon(world, state, pos, entity, fallDistance);
    }

    @Override
    @Deprecated
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        baseBlock.onEntityCollision(state, world, pos, entity);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!baseBlock.canInteract(state, world, pos, EquineContext.of(context))) {
            return VoxelShapes.empty();
        }
        return super.getOutlineShape(state, world, pos, context);
    }

    @Override
    @Deprecated
    public final VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return super.getOutlineShape(state, world, pos, ShapeContext.absent());
    }

    @Override
    @Deprecated
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.collidable ? state.getOutlineShape(world, pos, context) : VoxelShapes.empty();
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        EquineContext equineContext = EquineContext.of(context);
        if (!baseBlock.canInteract(getDefaultState(), context.getWorld(), context.getBlockPos(), equineContext)) {
            return null;
        }
        return super.getPlacementState(context);
    }

    @Deprecated
    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return baseBlock.canReplace(state, context);
    }

    @Deprecated
    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return baseBlock.isSideInvisible(state, stateFrom, direction);
    }

    @Override
    @Deprecated
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getSoggyState(int moisture) {
        return soggyBlock == null ? (baseBlock instanceof Soakable s ? s.getSoggyState(moisture) : null) : soggyBlock.get().getSoggyState(moisture);
    }

    @Override
    public int getMoisture(BlockState state) {
        return baseBlock instanceof Soakable s ? s.getMoisture(state) : 0;
    }
}
