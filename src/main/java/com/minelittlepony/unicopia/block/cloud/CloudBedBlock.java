package com.minelittlepony.unicopia.block.cloud;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.block.FancyBedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudBedBlock extends FancyBedBlock implements CloudLike {
    private final BlockState baseState;
    private final CloudBlock baseBlock;

    public CloudBedBlock(String base, BlockState baseState, Settings settings) {
        super(base, settings.dynamicBounds());
        this.baseState = baseState;
        this.baseBlock = (CloudBlock)baseState.getBlock();
    }

    @Override
    public final VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (!baseBlock.canInteract(baseState, world, pos, EquineContext.of(context))) {
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
    public final BlockState getPlacementState(ItemPlacementContext context) {
        if (!baseBlock.canInteract(baseState, context.getWorld(), context.getBlockPos(), EquineContext.of(context))) {
            return null;
        }
        return super.getPlacementState(context);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!baseBlock.canInteract(baseState, world, pos, EquineContext.of(player))) {
            return ActionResult.PASS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Deprecated
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        baseState.onEntityCollision(world, pos, entity);
    }

    @Override
    @Deprecated
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return true;
    }
}
