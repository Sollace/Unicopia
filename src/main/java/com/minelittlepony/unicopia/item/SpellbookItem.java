package com.minelittlepony.unicopia.item;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.advancement.UCriteria;
import com.minelittlepony.unicopia.entity.mob.SpellbookEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.server.world.Altar;
import com.minelittlepony.unicopia.util.Dispensable;

import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpellbookItem extends BookItem implements Dispensable {
    public SpellbookItem(Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, createDispenserBehaviour());
    }

    @Override
    public TypedActionResult<ItemStack> dispenseStack(BlockPointer pointer, ItemStack stack) {
        Direction facing = pointer.getBlockState().get(DispenserBlock.FACING);
        Position pos = DispenserBlock.getOutputLocation(pointer);

        float yaw = facing.getOpposite().asRotation();
        if (placeBook(stack, pointer.getWorld(), pos, yaw, null)) {
            stack.decrement(1);

            return new TypedActionResult<>(ActionResult.SUCCESS, stack);
        }

        return new TypedActionResult<>(ActionResult.FAIL, stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {

        @Nullable
        PlayerEntity player = context.getPlayer();

        if (!context.getWorld().isClient) {
            BlockPos pos = context.getBlockPos().offset(context.getSide());

            if (placeBook(context.getStack(), context.getWorld(), Vec3d.ofBottomCenter(pos), context.getPlayerYaw() + 180, player)) {
                if (!player.getAbilities().creativeMode) {
                    player.getStackInHand(context.getHand()).decrement(1);
                }

                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }

    private static boolean placeBook(ItemStack stack, World world, Position pos, float yaw, @Nullable Entity placer) {
        SpellbookEntity book = UEntities.SPELLBOOK.create(world);

        book.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
        book.setHeadYaw(yaw);
        book.setYaw(yaw);

        if (!book.canSpawn(world)) {
            return false;
        }

        @Nullable
        NbtCompound tag = stack.getSubNbt("spellbookState");
        if (tag != null) {
            book.getSpellbookState().fromNBT(tag);
        }

        world.spawnEntity(book);

        Altar.locateAltar(world, book.getBlockPos()).ifPresent(altar -> {
            book.setAltar(altar);
            altar.generateDecorations(world);
            UCriteria.LIGHT_ALTAR.trigger(placer);
        });

        return true;
    }
}















