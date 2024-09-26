package com.minelittlepony.unicopia.block.cloud;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.container.ShapingBenchScreenHandler;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ShapingBenchBlock extends CloudBlock {
    private static final MapCodec<ShapingBenchBlock> CODEC = Block.createCodec(ShapingBenchBlock::new);
    private static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0, 13, 0, 3, 18, 3),
            Block.createCuboidShape(13, 13, 0, 16, 18, 3),
            Block.createCuboidShape(0, 13, 13, 3, 18, 16),
            Block.createCuboidShape(13, 13, 13, 16, 18, 16),
            Block.createCuboidShape(0, 13, 0, 16, 16, 16),
            Block.createCuboidShape(2, 0, 2, 14, 17, 14),
            Block.createCuboidShape(0, 0, 0, 16, 4, 16)
    );

    public ShapingBenchBlock(Settings settings) {
        super(false, settings);
    }

    @Override
    public MapCodec<ShapingBenchBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, EquineContext equineContext) {
        return SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        player.incrementStat(Stats.INTERACT_WITH_STONECUTTER);
        return ActionResult.CONSUME;
    }

    @Override
    @Nullable
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
            return new ShapingBenchScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, pos));
        }, getName());
    }
}
