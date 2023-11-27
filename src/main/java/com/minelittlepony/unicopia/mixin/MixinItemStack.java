package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.item.toxin.ToxicHolder;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

@Mixin(ItemStack.class)
abstract class MixinItemStack {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        ItemStack self = (ItemStack)(Object)this;
        TypedActionResult<ItemStack> result = ((ToxicHolder)self.getItem()).getToxic(self, user).startUsing(self, world, user, hand);
        if (result.getResult() != ActionResult.PASS) {
            info.setReturnValue(result);
        }
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        ItemStack self = (ItemStack)(Object)this;
        ((ToxicHolder)self.getItem()).getToxic(self, user).finishUsing(self, world, user);
    }
}
