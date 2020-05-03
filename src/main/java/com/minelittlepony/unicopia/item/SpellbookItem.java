package com.minelittlepony.unicopia.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.entity.SpellbookEntity;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.magic.Dispensable;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BookItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SpellbookItem extends BookItem {

    public SpellbookItem(Settings settings) {
        super(settings);
        Dispensable.setDispenseable(this, (source, stack) -> {
            Direction facing = source.getBlockState().get(DispenserBlock.FACING);
            BlockPos pos = source.getBlockPos().offset(facing);

            float yaw = facing.getOpposite().asRotation();
            placeBook(source.getWorld(), pos.getX(), pos.getY(), pos.getZ(), yaw);
            stack.decrement(1);

            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        });
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        @Nullable
        PlayerEntity player = context.getPlayer();

        if (!context.getWorld().isClient && EquinePredicates.PLAYER_UNICORN.test(player)) {
            BlockPos pos = context.getBlockPos().offset(context.getSide());

            placeBook(context.getWorld(), pos.getX(), pos.getY(), pos.getZ(), context.getPlayerYaw() + 180);

            if (!player.abilities.creativeMode) {
                player.getStackInHand(context.getHand()).decrement(1);
            }

            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static void placeBook(World world, int x, int y, int z, float yaw) {
        SpellbookEntity book = UEntities.SPELLBOOK.create(world);

        book.refreshPositionAndAngles(x + 0.5, y, z + 0.5, 0, 0);
        book.setHeadYaw(yaw);
        book.setYaw(yaw);
        world.spawnEntity(book);
    }
}















