package com.minelittlepony.unicopia.magic.items;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;

public interface IDispensable {
    /**
     * Enables dispensing behaviours for this item.
     */
    default Item setDispenseable() {
        DispenserBlock.registerBehavior((Item)this, new ItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer source, ItemStack stack) {
                TypedActionResult<ItemStack> result = dispenseStack(source, stack);

                if (result.getResult() != ActionResult.SUCCESS) {
                    return super.dispense(source, stack);
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
}
