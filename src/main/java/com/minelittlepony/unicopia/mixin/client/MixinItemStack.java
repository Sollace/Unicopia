package com.minelittlepony.unicopia.mixin.client;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.diet.DietView;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
abstract class MixinItemStack {
    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void onGetTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> info) {
        ItemStack self = (ItemStack)(Object)this;
        ((DietView.Holder)self.getItem()).getDiets(self).appendTooltip(self, player, info.getReturnValue(), context);
    }
}
