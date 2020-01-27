package com.minelittlepony.unicopia.redux.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.magic.IDispensable;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ExtendedShearsItem extends ShearsItem {

    @Nullable
    private static DispenserBehavior vanillaDispenserBehaviour;
    private static final DispenserBehavior dispenserBehavior = new ItemDispenserBehavior() {
        @Override
        protected ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {

            Direction facing = source.getBlockState().get(DispenserBlock.FACING);
            BlockPos pos = source.getBlockPos().offset(facing);
            World w = source.getWorld();

            if (UItems.moss.tryConvert(w, w.getBlockState(pos), pos, null)) {
                stack.damage(1, w.random, null);

                return stack;
            }

            if (vanillaDispenserBehaviour != null) {
                return vanillaDispenserBehaviour.dispense(source, stack);
            }

            return stack;
        }
    };

    public ExtendedShearsItem() {
        super(new Item.Settings().maxDamage(238).group(ItemGroup.TOOLS));

        vanillaDispenserBehaviour = IDispensable.getBehaviorForItem(new ItemStack(Items.SHEARS));
        DispenserBlock.registerBehavior(Items.SHEARS, dispenserBehavior);
        DispenserBlock.registerBehavior(this, dispenserBehavior);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());

        PlayerEntity player = context.getPlayer();

        if (UItems.moss.tryConvert(context.getWorld(), state, context.getBlockPos(), context.getPlayer())) {
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
