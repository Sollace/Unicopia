package com.minelittlepony.unicopia.item;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;

public interface IDispensable {
    IBehaviorDispenseItem dispenserBehavior = new BehaviorDefaultDispenseItem() {
        @Override
        protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {

            ActionResult<ItemStack> result = ((IDispensable)stack.getItem()).dispenseStack(source, stack);

            if (result.getType() != EnumActionResult.SUCCESS) {
                return super.dispense(source, stack);
            }

            return result.getResult();
        }
    };

    /**
     * Enables dispensing behaviours for this item.
     */
    default Item setDispenseable() {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject((Item)(Object)this, dispenserBehavior);

        return (Item)(Object)this;
    }

    /**
     * Called to dispense this stack.
     */
    ActionResult<ItemStack> dispenseStack(IBlockSource source, ItemStack stack);
}
