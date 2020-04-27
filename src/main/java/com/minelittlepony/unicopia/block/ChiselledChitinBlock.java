package com.minelittlepony.unicopia.block;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class ChiselledChitinBlock extends Block {

    public ChiselledChitinBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState()
                .with(Properties.FACING, Direction.UP)
        );
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        return state.with(Properties.FACING, rot.rotate(state.get(Properties.FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.with(Properties.FACING, mirror.apply(state.get(Properties.FACING)));
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(Properties.FACING, context.getPlayerFacing());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.FACING);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView worldIn, BlockPos pos) {
        float hardness = super.calcBlockBreakingDelta(state, player, worldIn, pos);

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
}
