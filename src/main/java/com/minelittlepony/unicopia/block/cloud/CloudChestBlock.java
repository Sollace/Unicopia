package com.minelittlepony.unicopia.block.cloud;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.block.UBlockEntities;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudChestBlock extends ChestBlock implements CloudLike {
    private static final MapCodec<CloudChestBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockState.CODEC.fieldOf("base_state").forGetter(block -> block.baseState),
            StairsBlock.createSettingsCodec()
    ).apply(instance, CloudChestBlock::new));
    private final BlockState baseState;
    private final CloudBlock baseBlock;

    private static final DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<NamedScreenHandlerFactory>> NAME_RETRIEVER = new DoubleBlockProperties.PropertyRetriever<>(){
        @Override
        public Optional<NamedScreenHandlerFactory> getFromBoth(final ChestBlockEntity first, final ChestBlockEntity second) {
            final DoubleInventory inventory = new DoubleInventory(first, second);
            return Optional.of(new NamedScreenHandlerFactory(){
                @Override
                @Nullable
                public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity player) {
                    if (first.checkUnlocked(player) && second.checkUnlocked(player)) {
                        first.generateLoot(playerInventory.player);
                        second.generateLoot(playerInventory.player);
                        return GenericContainerScreenHandler.createGeneric9x6(i, playerInventory, inventory);
                    }
                    return null;
                }

                @Override
                public Text getDisplayName() {
                    if (first.hasCustomName()) {
                        return first.getDisplayName();
                    }
                    if (second.hasCustomName()) {
                        return second.getDisplayName();
                    }
                    return Text.translatable(first.getCachedState().getBlock().getTranslationKey() + ".double");
                }
            });
        }

        @Override
        public Optional<NamedScreenHandlerFactory> getFrom(ChestBlockEntity chest) {
            return Optional.of(chest);
        }

        @Override
        public Optional<NamedScreenHandlerFactory> getFallback() {
            return Optional.empty();
        }
    };

    public CloudChestBlock(BlockState baseState, Settings settings) {
        super(settings, () -> UBlockEntities.CLOUD_CHEST);
        this.baseState = baseState;
        this.baseBlock = (CloudBlock)baseState.getBlock();
    }

    @Override
    public MapCodec<? extends ChestBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TileData(pos, state);
    }

    @Override
    @Nullable
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return getBlockEntitySource(state, world, pos, false).apply(NAME_RETRIEVER).orElse(null);
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

    public static class TileData extends ChestBlockEntity {
        protected TileData(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
            super(blockEntityType, blockPos, blockState);
        }

        public TileData(BlockPos pos, BlockState state) {
            super(UBlockEntities.CLOUD_CHEST, pos, state);
        }

        @Override
        protected Text getContainerName() {
            return getCachedState().getBlock().getName();
        }
    }
}
