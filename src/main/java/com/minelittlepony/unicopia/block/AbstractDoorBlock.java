package com.minelittlepony.unicopia.block;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.util.WorldEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.Material;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class AbstractDoorBlock extends DoorBlock {

    protected AbstractDoorBlock(Settings settings) {
        super(settings);
    }

    protected WorldEvent getCloseSound() {
        return isLockable() ? WorldEvent.IRON_DOOR_SLAM : WorldEvent.WOODEN_DOOR_SLAM;
    }

    protected WorldEvent getOpenSound() {
        return isLockable() ? WorldEvent.IRON_DOOR_OPEN : WorldEvent.WOODEN_DOOR_OPEN;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return toggleDoor(world, pos, false, true, player);
    }

    @Override
    public void setOpen(World world, BlockPos pos, boolean open) {
        toggleDoor(world, pos, open, false, null);
    }

    protected boolean isLockable() {
        return material == Material.METAL;
    }

    protected boolean canBePowered() {
        return true;
    }

    protected boolean canOpen(@Nullable PlayerEntity player) {
        return player == null || material != Material.METAL;
    }

    protected ActionResult toggleDoor(World world, BlockPos pos, boolean open, boolean force, @Nullable PlayerEntity player) {
        if (!canOpen(player)) {
            return ActionResult.PASS;
        }

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() != this) {
            return ActionResult.PASS;
        }

        BlockPos lower = getPrimaryDoorPos(state, pos);

        BlockState mainDoor = pos == lower ? state : world.getBlockState(lower);

        if (mainDoor.getBlock() != this) {
            return ActionResult.PASS;
        }

        if (!force && mainDoor.get(OPEN) == open) {
            return ActionResult.FAIL;
        }

        state = mainDoor.cycle(OPEN);

        world.setBlockState(lower, state, 10);

        WorldEvent sound = state.get(OPEN) ? getOpenSound() : getCloseSound();

        world.playLevelEvent(player, sound.getId(), pos, 0);

        return ActionResult.SUCCESS;
    }

    protected BlockPos getPrimaryDoorPos(BlockState state, BlockPos pos) {
        return state.get(HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sender, BlockPos blockPos_2, boolean boolean_1) {
        if (!canBePowered()) {
            return;
        }

        BlockPos otherHalf = pos.offset(state.get(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN);

        boolean powered = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(otherHalf);

        if (sender != this && powered != state.get(POWERED)) {
            world.setBlockState(pos, state.with(POWERED, powered), 2);

            if (onPowerStateChanged(world, state.with(POWERED, powered), pos, powered)) {
                playToggleSound(world, pos, powered);
            }
        }
    }

    protected void playToggleSound(World world, BlockPos pos, boolean powered) {
        world.playLevelEvent(null, (powered ? getOpenSound() : getCloseSound()).getId(), pos, 0);
    }

    /**
     * Called by the lower block when the powered state changes.
     */
    protected boolean onPowerStateChanged(World world, BlockState state, BlockPos pos, boolean powered) {
        if (powered != state.get(OPEN)) {
            world.setBlockState(pos, state.with(OPEN, powered), 2);

            return true;
        }

        return false;
    }
}
