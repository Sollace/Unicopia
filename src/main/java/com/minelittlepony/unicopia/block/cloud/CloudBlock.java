package com.minelittlepony.unicopia.block.cloud;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TransparentBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class CloudBlock extends TransparentBlock {

    protected final boolean meltable;

    public CloudBlock(Settings settings, boolean meltable) {
        super((meltable ? settings.ticksRandomly() : settings).nonOpaque());
        this.meltable = meltable;
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        boolean bounce = Math.abs(entity.getVelocity().y) > 0.3;
        super.onEntityLand(world, entity);
        if (bounce) {
            entity.addVelocity(0, 0.2F, 0);
        }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.handleFallDamage(fallDistance, 0, world.getDamageSources().fall());
    }

    @Override
    @Deprecated
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {

        if (entity instanceof PlayerEntity player && (player.getAbilities().flying || Pony.of(player).getPhysics().isFlying())) {
            return;
        }

        if (entity.getVelocity().y < 0) {
            float cloudWalking = EquineContext.of(entity).getCloudWalkingStrength();
            if (cloudWalking > 0) {
                entity.setVelocity(entity.getVelocity().multiply(1, 1 - cloudWalking, 1));
                entity.addVelocity(0, 0.07, 0);
                entity.setOnGround(true);
            }
            entity.setVelocity(entity.getVelocity().multiply(0.9F, 1, 0.9F));
        } else {
            entity.setVelocity(entity.getVelocity().multiply(0.9F));
        }
    }

    @Override
    public final VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        EquineContext equineContext = EquineContext.of(context);
        if (!canInteract(state, world, pos, equineContext)) {
            return VoxelShapes.empty();
        }
        return getOutlineShape(state, world, pos, context, equineContext);
    }

    @Override
    @Deprecated
    public final VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return getOutlineShape(state, world, pos, ShapeContext.absent(), EquineContext.ABSENT);
    }

    @Override
    @Deprecated
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.collidable ? state.getOutlineShape(world, pos, context) : VoxelShapes.empty();
    }

    @Override
    @Nullable
    public final BlockState getPlacementState(ItemPlacementContext context) {
        EquineContext equineContext = EquineContext.of(context);
        if (!canInteract(getDefaultState(), context.getWorld(), context.getBlockPos(), equineContext)) {
            return null;
        }
        return getPlacementState(context, equineContext);
    }

    @Deprecated
    @Override
    public final boolean canReplace(BlockState state, ItemPlacementContext context) {
        EquineContext equineContext = EquineContext.of(context);
        if (canInteract(state, context.getWorld(), context.getBlockPos(), equineContext)) {
            return canReplace(state, context, equineContext);
        }
        return true;
    }

    @Deprecated
    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        VoxelShape shape = state.getCullingShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
        VoxelShape shapeFrom = stateFrom.getCullingShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
        return !shape.isEmpty() && !shapeFrom.isEmpty() && VoxelShapes.isSideCovered(shape, shapeFrom, direction);
    }

    @Override
    @Deprecated
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return true;
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, EquineContext equineContext) {
        return VoxelShapes.fullCube();
    }

    protected boolean canInteract(BlockState state, BlockView world, BlockPos pos, EquineContext context) {
        return context.getCompositeRace().any(Race::canInteractWithClouds);
    }

    @SuppressWarnings("deprecation")
    protected boolean canReplace(BlockState state, ItemPlacementContext context, EquineContext equineContext) {
        return super.canReplace(state, context);
    }

    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext placementContext, EquineContext equineContext) {
        return super.getPlacementState(placementContext);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (meltable) {
            if (world.getLightLevel(LightType.BLOCK, pos) > 11) {
                dropStacks(state, world, pos);
                world.removeBlock(pos, false);
            }
        }
    }
}
