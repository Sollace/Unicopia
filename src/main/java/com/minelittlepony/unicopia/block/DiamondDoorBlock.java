package com.minelittlepony.unicopia.block;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DiamondDoorBlock extends AbstractDoorBlock {
    public DiamondDoorBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean canOpen(@Nullable PlayerEntity player) {
        return EquinePredicates.PLAYER_UNICORN.test(player);
    }

    @Override
    protected boolean onPowerStateChanged(World world, BlockState state, BlockPos pos, boolean powered) {
        if (state.get(OPEN)) {
            world.setBlockState(pos, state.with(OPEN, false), 2);

            return true;
        }

        return false;
    }
}
