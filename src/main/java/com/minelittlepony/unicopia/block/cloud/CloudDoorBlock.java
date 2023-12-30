package com.minelittlepony.unicopia.block.cloud;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
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

public class CloudDoorBlock extends DoorBlock implements CloudLike {
    private final BlockState baseState;
    private final CloudBlock baseBlock;

    public CloudDoorBlock(Settings settings, BlockState baseState, BlockSetType blockSet) {
        super(blockSet, settings);
        this.baseState = baseState;
        this.baseBlock = (CloudBlock)baseState.getBlock();
    }


    @Override
    public final VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (canPassThrough(state, world, pos, EquineContext.of(context))) {
            return VoxelShapes.empty();
        }
        return super.getOutlineShape(state, world, pos, context);
    }

    protected boolean canPassThrough(BlockState state, BlockView world, BlockPos pos, EquineContext context) {
        return context.getCompositeRace().any(Race::canUseEarth);
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

        EquineContext context = EquineContext.of(entity);

        if (!baseBlock.canInteract(baseState, world, pos, context)) {
            entity.setVelocity(entity.getVelocity().multiply(0.5F, 1, 0.5F));
        }
    }
}
