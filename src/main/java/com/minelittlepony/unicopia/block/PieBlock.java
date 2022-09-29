package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.util.SoundEmitter;

import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.event.GameEvent;

public class PieBlock extends Block {
    public static final int MAX_BITES = 3;
    public static final IntProperty BITES = IntProperty.of("bites", 0, MAX_BITES);
    private static final VoxelShape[] SHAPES;
    static {
        final int PIE_HEIGHT = 5;
        final VoxelShape WEDGE = Block.createCuboidShape(1, 0, 1, 8, PIE_HEIGHT, 8);
        final float OFFSET_AMOUNT = 7F/16F;
        SHAPES = new VoxelShape[] {
                Block.createCuboidShape(1, 0, 1, 15, PIE_HEIGHT, 15),
                VoxelShapes.union(WEDGE, WEDGE.offset(OFFSET_AMOUNT, 0, 0), WEDGE.offset(OFFSET_AMOUNT, 0, OFFSET_AMOUNT)),
                VoxelShapes.union(WEDGE, WEDGE.offset(OFFSET_AMOUNT, 0, 0)),
                WEDGE
            };
    }

    public PieBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(BITES, 0));
    }

    @Deprecated
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES[state.get(BITES)];
    }

    @Deprecated
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack itemStack = player.getStackInHand(hand);

        if (world.isClient) {
            if (tryEat(world, pos, state, player).isAccepted()) {
                return ActionResult.SUCCESS;
            }

            if (itemStack.isEmpty()) {
                return ActionResult.CONSUME;
            }
        }

        return tryEat(world, pos, state, player);
    }

    protected ActionResult tryEat(WorldAccess world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!player.canConsume(false)) {
            return ActionResult.PASS;
        }
        player.incrementStat(Stats.EAT_CAKE_SLICE);
        player.getHungerManager().add(2, 0.1f);
        int bites = state.get(BITES);
        world.emitGameEvent(player, GameEvent.EAT, pos);
        SoundEmitter.playSoundAt(player, SoundEvents.ENTITY_PLAYER_BURP, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);

        if (bites < MAX_BITES) {
            world.setBlockState(pos, state.with(BITES, bites + 1), Block.NOTIFY_ALL);
        } else {
            world.removeBlock(pos, false);
            world.emitGameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
        return ActionResult.SUCCESS;
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Deprecated
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).getMaterial().isSolid();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return (5 - state.get(BITES)) * 2;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
