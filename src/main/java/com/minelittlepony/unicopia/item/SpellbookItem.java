package com.minelittlepony.unicopia.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.SpellbookEntity;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.magic.Dispensable;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SpellbookItem extends BookItem {

    public SpellbookItem() {
        super(new Item.Settings()
                .maxCount(1)
                .group(ItemGroup.BREWING));
        Dispensable.setDispenseable(this, (source, stack) -> {
            Direction facing = source.getBlockState().get(DispenserBlock.FACING);
            BlockPos pos = source.getBlockPos().offset(facing);

            int yaw = facing.getOpposite().getHorizontal() * 90;
            placeBook(source.getWorld(), pos.getX(), pos.getY(), pos.getZ(), yaw);
            stack.decrement(1);

            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        });
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        @Nullable
        PlayerEntity player = context.getPlayer();

        if (!context.getWorld().isClient && EquinePredicates.MAGI.test(player)) {
            BlockPos pos = context.getBlockPos().offset(context.getSide());

            double diffX = player.getX() - (pos.getX() + 0.5);
            double diffZ = player.getZ() - (pos.getZ() + 0.5);
            float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX) + Math.PI);

            placeBook(context.getWorld(), pos.getX(), pos.getY(), pos.getZ(), yaw);

            if (!player.abilities.creativeMode) {
                player.getStackInHand(context.getHand()).decrement(1);
            }

            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static void placeBook(World world, int x, int y, int z, float yaw) {
        SpellbookEntity book = UEntities.SPELLBOOK.create(world);

        book.updatePositionAndAngles(x + 0.5, y, z + 0.5, yaw, 0);
        book.prevYaw = yaw;

        world.spawnEntity(book);
    }
}















