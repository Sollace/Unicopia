package com.minelittlepony.unicopia.mixin.trinkets;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

// fixed shift-clicking (handles other 10%)
@Mixin(ScreenHandler.class)
abstract class MixinScreenHandler {
    @Nullable
    private Slot currentSlot;

    @Redirect(method = "insertItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;"
            )
    )
    // manual capture of the current slot since @Redirect doesn't support local captures
    private Object onGetSlot(DefaultedList<Slot> sender, int index) {
        currentSlot = sender.get(index);
        return currentSlot;
    }

    @Redirect(method = "insertItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/Slot;getMaxItemCount()I"
            )
    )
    // redirect slot.getMaxItemCount() to stack aware version
    protected int onGetMaxItemCount(Slot sender, ItemStack stack) {
        return TrinketsDelegate.getInstance(null).isTrinketSlot(sender) ? sender.getMaxItemCount(stack) : sender.getMaxItemCount();
    }

    @Redirect(method = "insertItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
                    ordinal = 1
            )
    )
    // redirect "if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack))" -> "if (!canNotInsert(itemStack, slot) && ItemStack.canCombine(stack, itemStack))"
    protected boolean canNotInsert(ItemStack sender) {
        return sender.isEmpty() || (TrinketsDelegate.getInstance(null).isTrinketSlot(currentSlot) && (currentSlot.getStack().getCount() + sender.getCount()) <= currentSlot.getMaxItemCount(sender));
    }

    @Redirect(method = "canInsertItemIntoSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"
            )
    )
    private static int onGetMaxCount(ItemStack sender, @Nullable Slot slot) {
        return TrinketsDelegate.getInstance(null).isTrinketSlot(slot) ? slot.getMaxItemCount(sender) : sender.getMaxCount();
    }
}
