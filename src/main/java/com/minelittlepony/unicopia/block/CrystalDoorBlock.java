package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class CrystalDoorBlock extends DoorBlock {

    public CrystalDoorBlock(Settings settings, BlockSetType blockSet) {
        super(settings, blockSet);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean powered = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(pos.offset(state.get(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
        if (!getDefaultState().isOf(sourceBlock) && powered != state.get(POWERED)) {
            if (powered) {
                state = state.cycle(OPEN);
                playOpenCloseSound(null, world, pos, state.get(OPEN));
                world.emitGameEvent(null, state.get(OPEN) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }

            world.setBlockState(pos, state.with(POWERED, powered), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!EquineContext.of(player).getCompositeRace().any(Race::canCast)) {

            if (!player.getStackInHand(hand).isOf(UItems.MEADOWBROOKS_STAFF)) {
                playOpenCloseSound(player, world, pos, false);
                return ActionResult.FAIL;
            } else {
                world.playSound(player, pos, USounds.ENTITY_CRYSTAL_SHARDS_AMBIENT, SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1F + 0.9F);
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    private void playOpenCloseSound(@Nullable Entity entity, World world, BlockPos pos, boolean open) {
        world.playSound(entity, pos, open ? getBlockSetType().doorOpen() : getBlockSetType().doorClose(), SoundCategory.BLOCKS, 1, world.getRandom().nextFloat() * 0.1f + 0.9f);
    }

}
