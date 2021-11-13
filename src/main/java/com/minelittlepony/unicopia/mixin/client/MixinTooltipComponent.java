package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;

@Mixin(TooltipComponent.class)
interface MixinTooltipComponent {
    @Inject(method = "of", at = @At("HEAD"), cancellable = true)
    private static void onOf(OrderedText text, CallbackInfoReturnable<TooltipComponent> info) {
        if (text instanceof TooltipComponent) {
            info.setReturnValue((TooltipComponent)text);
        }
    }
}
