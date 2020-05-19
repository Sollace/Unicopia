package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.block.StickPlantBlock;
import com.minelittlepony.unicopia.block.UBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TomatoSeedsItem extends Item {

    public TomatoSeedsItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(context.getBlockPos());

        Block block = state.getBlock();

        if (block instanceof StickPlantBlock && (block == UBlocks.TOMATO_PLANT || block == UBlocks.CLOUDSDALE_TOMATO_PLANT)) {
            StickPlantBlock plant = (StickPlantBlock)block;

            if (plant.getSeedsItem() == this && state.get(plant.getAgeProperty()) == 0 && world.setBlockState(pos, plant.getPlacedState(world, pos, state).with(plant.getAgeProperty(), 1), 11)) {
                BlockSoundGroup sound = block.getSoundGroup(state);

                context.getWorld().playSound(null, pos, sound.getPlaceSound(), SoundCategory.BLOCKS, sound.getVolume(), sound.getPitch() * 2);

                PlayerEntity player = context.getPlayer();

                if (player == null || !player.isCreative()) {
                    context.getStack().decrement(1);
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }
}
