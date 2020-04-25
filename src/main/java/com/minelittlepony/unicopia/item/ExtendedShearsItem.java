package com.minelittlepony.unicopia.item;

import java.util.Optional;

import com.minelittlepony.unicopia.magic.Dispensable;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExtendedShearsItem extends ShearsItem {

    public ExtendedShearsItem() {
        super(new Item.Settings().maxDamage(238).group(ItemGroup.TOOLS));
        final Optional<DispenserBehavior> vanillaDispenserBehaviour = Dispensable.getBehavior(new ItemStack(Items.SHEARS));
        DispenserBlock.registerBehavior(Items.SHEARS, Dispensable.setDispenseable(this, (source, stack) -> {

            BlockPos pos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
            World w = source.getWorld();

            if (UItems.MOSS.tryConvert(w, w.getBlockState(pos), pos, null)) {
                stack.damage(1, w.random, null);

                return TypedActionResult.success(stack);
            }

            return vanillaDispenserBehaviour
                    .map(action -> {
                        return TypedActionResult.pass(action.dispense(source, stack));
                    })
                    .orElseGet(() -> TypedActionResult.fail(stack));
        }));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());

        PlayerEntity player = context.getPlayer();

        if (UItems.MOSS.tryConvert(context.getWorld(), state, context.getBlockPos(), context.getPlayer())) {
            if (player != null) {
                ItemStack stack = context.getStack();

                if (player == null || !player.isCreative()) {
                    stack.damage(1, player, p -> p.sendToolBreakStatus(context.getHand()));
                }
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
