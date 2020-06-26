package com.minelittlepony.unicopia.world.block.gas;

import com.minelittlepony.unicopia.world.block.AbstractDoorBlock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudDoorBlock extends AbstractDoorBlock implements Gas {
    public CloudDoorBlock(Settings settings) {
        super(settings);
    }

    @Override
    public GasState getGasState(BlockState blockState) {
        return GasState.NORMAL;
    }

    @Override
    public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!getGasState(state).canTouch(player)) {
            return ActionResult.PASS;
        }
        return super.onUse(state, worldIn, pos, player, hand, hit);
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(state, w, pos, entity);
        }
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (GasState.NORMAL.canTouch(player)) {
            float f = state.getHardness(world, pos);
            f = Math.max(f, Math.min(60, f + (pos.getY() - 100)));
            if (f == -1) {
                return 0;
            }

            float toolBreakingSpeedUp = player.isUsingEffectiveTool(state) ? 30 : 100;
            return player.getBlockBreakingSpeed(state) / f / toolBreakingSpeedUp;
        }
        return -1;
    }
}
