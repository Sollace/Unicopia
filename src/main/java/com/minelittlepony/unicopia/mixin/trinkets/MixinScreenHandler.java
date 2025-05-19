package com.minelittlepony.unicopia.mixin.trinkets;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

// fixed shift-clicking (handles other 10%)
@Mixin(ScreenHandler.class)
abstract class MixinScreenHandler {
    @ModifyExpressionValue(
        method = "canInsertItemIntoSlot",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I")
    )
    private static int modifyMaxCount(int maxCount, @Nullable Slot slot, ItemStack stack, boolean allowOverflow) {
        return slot.getMaxItemCount(stack);
    }
}
