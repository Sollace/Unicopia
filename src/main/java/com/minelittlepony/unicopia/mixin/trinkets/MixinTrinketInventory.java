package com.minelittlepony.unicopia.mixin.trinkets;

import org.spongepowered.asm.mixin.*;

import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegateImpl;

import dev.emi.trinkets.api.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

@Mixin(TrinketInventory.class)
abstract class MixinTrinketInventory implements Inventory {

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        ItemStack existingStack = getStack(slot);
        SlotReference ref = new SlotReference((TrinketInventory)(Object)this, slot);

        int max = Math.min(
                existingStack.isEmpty() ? 64 : TrinketsDelegateImpl.getMaxCount(existingStack, ref, existingStack.getMaxCount()),
                stack.isEmpty() ? 64 : TrinketsDelegateImpl.getMaxCount(stack, ref, stack.getMaxCount())
        );

        int combinedCount = stack.getCount();

        if (ItemStack.areItemsAndComponentsEqual(existingStack, stack)) {
            combinedCount += existingStack.getCount();
        }

        return combinedCount <= max;
    }

}
