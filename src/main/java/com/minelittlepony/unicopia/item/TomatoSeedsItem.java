package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.block.StickBlock;
import com.minelittlepony.unicopia.block.UBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public class TomatoSeedsItem extends Item {

    public TomatoSeedsItem() {
        super(new Settings().group(ItemGroup.MATERIALS));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        BlockState state = context.getWorld().getBlockState(context.getBlockPos());

        Block block = state.getBlock();

        if (block instanceof StickBlock) {
            if (UBlocks.tomato_plant.plant(context.getWorld(), context.getBlockPos(), state)) {
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
