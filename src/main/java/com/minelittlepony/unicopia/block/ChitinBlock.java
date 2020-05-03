package com.minelittlepony.unicopia.block;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;

public class ChitinBlock extends Block {

    public ChitinBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState().with(Covering.PROPERTY, Covering.UNCOVERED));
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float hardness = super.calcBlockBreakingDelta(state, player, world, pos);

        Pony iplayer = Pony.of(player);
        Race race = iplayer.getSpecies();

        if (race == Race.CHANGELING) {
            hardness *= 80;
        } else if (race.canInteractWithClouds()) {
            hardness /= 4;
        } else if (race.canUseEarth()) {
            hardness *= 10;
        }

        return hardness;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(Covering.PROPERTY, Covering.getCovering(context.getWorld(), context.getBlockPos().up()));
    }

    @Override
    @Deprecated
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState other, IWorld world, BlockPos pos, BlockPos otherPos) {
        if (direction == Direction.UP) {
            return state.with(Covering.PROPERTY, Covering.getCovering(world, otherPos));
        }

        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Covering.PROPERTY);
    }
}
