package com.minelittlepony.unicopia.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Mixin(EnchantmentHelper.class)
abstract class MixinEnchantmentHelper {
    @Inject(method = "getEquipmentLevel", at = @At("RETURN"), cancellable = true)
    private static void getEquipmentLevel(Enchantment enchantment, LivingEntity entity, CallbackInfoReturnable<Integer> info) {
        Pony.of(entity).ifPresent(pony -> {
            int initial = info.getReturnValue();
            int implicitLevel = pony.getImplicitEnchantmentLevel(enchantment, initial);
            if (implicitLevel != initial) {
                info.setReturnValue(implicitLevel);
            }
        });
    }

    @Inject(method = "getPossibleEntries", at = @At("RETURN"))
    private static void onGetPossibleEntries(int power, ItemStack stack, boolean treasureAllowed, CallbackInfoReturnable<List<EnchantmentLevelEntry>> info) {
        info.getReturnValue().removeIf(entry -> !entry.enchantment.isAcceptableItem(stack));
    }
}
