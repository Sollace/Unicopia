package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.diet.DietView;
import com.minelittlepony.unicopia.item.DamageChecker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
        TypedActionResult<ItemStack> result = ((DietView.Holder)self.getItem()).getDiets(self).startUsing(self, world, user, hand);
        if (result.getResult() != ActionResult.PASS) {
            info.setReturnValue(result);
        }
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void onFinishUsing(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> info) {
        ItemStack self = (ItemStack)(Object)this;
        ((DietView.Holder)self.getItem()).getDiets(self).finishUsing(self, world, user);
    }

    @Inject(method = "takesDamageFrom", at = @At("HEAD"))
    private void onTakesDamageFrom(DamageSource source, CallbackInfoReturnable<Boolean> info) {
        ItemStack self = (ItemStack)(Object)this;
        if (self.getItem() instanceof DamageChecker checker) {
            info.setReturnValue(checker.takesDamageFrom(source));
        }
    }
}
