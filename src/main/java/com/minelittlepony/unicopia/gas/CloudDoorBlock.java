package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.block.AbstractDoorBlock;
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

    @Deprecated
    @Override
    public float getHardness(BlockState blockState, BlockView world, BlockPos pos) {
        float hardness = super.getHardness(blockState, world, pos);

        return Math.max(hardness, Math.min(60, hardness + (pos.getY() - 100)));
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(state, w, pos, entity);
        }
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView worldIn, BlockPos pos) {
        if (GasState.NORMAL.canTouch(player)) {
            return super.calcBlockBreakingDelta(state, player, worldIn, pos);
        }
        return -1;
    }
}
