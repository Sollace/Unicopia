package com.minelittlepony.unicopia.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.block.UBlocks;

import net.minecraft.advancement.criterion.Criterions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class StickItem extends Item {
    public StickItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        Direction facing = context.getSide();

        @Nullable
        PlayerEntity player = context.getPlayer();

        if (facing == Direction.UP && world.isAir(pos.up()) && (player == null || world.canPlayerModifyAt(player, pos.offset(facing)))) {

            world.setBlockState(pos.up(), UBlocks.STICK.getDefaultState());

            BlockSoundGroup sound = world.getBlockState(pos).getSoundGroup();

            world.playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1) / 2, sound.getPitch());

            ItemStack itemstack = context.getStack();

            if (player instanceof ServerPlayerEntity) {
                Criterions.PLACED_BLOCK.trigger((ServerPlayerEntity)player, pos.up(), itemstack);
            }

            if (player == null || !player.abilities.creativeMode) {
                itemstack.decrement(1);
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.FAIL;
    }
}
