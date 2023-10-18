package com.minelittlepony.unicopia.item.cloud;

import com.minelittlepony.unicopia.block.cloud.CloudBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CloudBlockItem
extends BlockItem {
    public CloudBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());

        if (state.getBlock() instanceof CloudBlock) {
            return super.useOnBlock(context);
        }

        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        ItemPlacementContext context = new ItemPlacementContext(user, hand, user.getStackInHand(hand), new BlockHitResult(
            user.getEyePos(),
            Direction.UP,
            BlockPos.ofFloored(user.getEyePos()),
            true
        ));

        ActionResult actionResult = place(context);

        if (actionResult.isAccepted()) {
            return TypedActionResult.success(context.getStack(), world.isClient);
        }

        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        return !checkStatePlacement() || state.canPlaceAt(context.getWorld(), context.getBlockPos());
    }
}