package com.minelittlepony.unicopia.item.cloud;

import com.minelittlepony.unicopia.InteractionManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CloudBlockItem extends BlockItem {
    public CloudBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return super.useOnBlock(context);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        InteractionManager.getInstance().sendPlayerLookAngles(user);
        Vec3d targetPos = user.getEyePos().add(user.getRotationVec(1).multiply(1, 1.5, 1).normalize().multiply(2));
        ItemPlacementContext context = new ItemPlacementContext(user, hand, user.getStackInHand(hand), new BlockHitResult(
            targetPos,
            Direction.UP,
            BlockPos.ofFloored(targetPos),
            true
        ));

        return place(context);
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        return !checkStatePlacement() || state.canPlaceAt(context.getWorld(), context.getBlockPos());
    }
}