package com.minelittlepony.unicopia.magic;

import net.minecraft.block.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.Material;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;

public interface Dispensable {
    /**
     * Enables dispensing behaviours for this item.
     */
    default Item setDispenseable() {
        DispenserBlock.registerBehavior((Item)this, new ItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
                TypedActionResult<ItemStack> result = dispenseStack(source, stack);

                if (result.getResult() != ActionResult.SUCCESS) {
                    return super.dispenseSilently(source, stack);
                }

                return result.getValue();
            }
        });

        return (Item)this;
    }

    /**
     * Called to dispense this stack.
     */
    TypedActionResult<ItemStack> dispenseStack(BlockPointer source, ItemStack stack);

    static DispenserBehavior getBehaviorForItem(ItemStack stack) {
        return DispenserAccess.INSTANCE.getBehaviorForItem(stack);
    }
}

class DispenserAccess extends DispenserBlock {
    static final DispenserAccess INSTANCE = new DispenserAccess();
    private DispenserAccess() {
        super(Block.Settings.of(Material.BUBBLE_COLUMN));
    }

    @Override
    public DispenserBehavior getBehaviorForItem(ItemStack stack) {
        return super.getBehaviorForItem(stack);
    }
}