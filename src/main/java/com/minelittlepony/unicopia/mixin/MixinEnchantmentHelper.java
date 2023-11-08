package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;

@Mixin(EnchantmentHelper.class)
abstract class MixinEnchantmentHelper {
    @Inject(method = "getEquipmentLevel", at = @At("RETURN"), cancellable = true)
    private static void getEquipmentLevel(Enchantment enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> info) {
        Pony.of(entity).ifPresent(pony -> {
            int implicitLevel = pony.getImplicitEnchantmentLevel(enchantment);
            if (implicitLevel > 0) {
                info.setReturnValue(implicitLevel + info.getReturnValue());
            }
        });
    }
}
