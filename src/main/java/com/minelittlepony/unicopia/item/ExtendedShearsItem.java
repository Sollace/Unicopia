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
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ExtendedShearsItem extends ShearsItem implements Dispensable {

    private final Optional<DispenserBehavior> vanillaDispenserBehaviour = getBehavior(new ItemStack(Items.SHEARS));

    public ExtendedShearsItem() {
        super(new Item.Settings().maxDamage(238).group(ItemGroup.TOOLS));
        setDispenseable();
        DispenserBlock.registerBehavior(Items.SHEARS, getBehavior(new ItemStack(this)).get());
    }

    @Override
    public TypedActionResult<ItemStack> dispenseStack(BlockPointer source, ItemStack stack) {
        Direction facing = source.getBlockState().get(DispenserBlock.FACING);
        BlockPos pos = source.getBlockPos().offset(facing);
        World w = source.getWorld();

        if (UItems.MOSS.tryConvert(w, w.getBlockState(pos), pos, null)) {
            stack.damage(1, w.random, null);

            return TypedActionResult.success(stack);
        }

        return vanillaDispenserBehaviour
                .map(action -> TypedActionResult.success(action.dispense(source, stack)))
                .orElseGet(() -> TypedActionResult.fail(stack));
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
