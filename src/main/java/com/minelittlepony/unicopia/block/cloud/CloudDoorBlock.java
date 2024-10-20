package com.minelittlepony.unicopia.block.cloud;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudDoorBlock extends DoorBlock implements CloudLike {
    private static final MapCodec<CloudDoorBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("base_state").forGetter(b -> b.baseState),
            BlockSetType.CODEC.fieldOf("block_set_type").forGetter(DoorBlock::getBlockSetType),
            DoorBlock.createSettingsCodec()
    ).apply(instance, CloudDoorBlock::new));
    private final BlockState baseState;
    private final CloudBlock baseBlock;

    public CloudDoorBlock(BlockState baseState, BlockSetType blockSet, Settings settings) {
        super(blockSet, CloudLike.applyCloudProperties(settings));
        this.baseState = baseState;
        this.baseBlock = (CloudBlock)baseState.getBlock();
    }

    @Override
    public MapCodec<? extends CloudDoorBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected final VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (canPassThrough(state, world, pos, EquineContext.of(context))) {
            return VoxelShapes.empty();
        }
        return super.getOutlineShape(state, world, pos, context);
    }

    protected boolean canPassThrough(BlockState state, BlockView world, BlockPos pos, EquineContext context) {
        return context.getCompositeRace().any(Race::canUseEarth);
    }

    @Override
    protected final VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return super.getOutlineShape(state, world, pos, ShapeContext.absent());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
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
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!baseBlock.canInteract(baseState, world, pos, EquineContext.of(player))) {
            return ItemActionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        baseState.onEntityCollision(world, pos, entity);

        EquineContext context = EquineContext.of(entity);

        if (!baseBlock.canInteract(baseState, world, pos, context)) {
            entity.setVelocity(entity.getVelocity().multiply(0.5F, 1, 0.5F));
        }
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return !InteractionManager.getInstance().getPathingEquineContext().collidesWithClouds() || super.canPathfindThrough(state, type);
    }
}
