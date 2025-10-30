package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;

@Mixin(EnchantmentHelper.class)
abstract class MixinEnchantmentHelper {
    @ModifyReturnValue(method = "getEquipmentLevel", at = @At("RETURN"))
    private static int onGetEquipmentLevel(int initial, RegistryEntry<Enchantment> enchantment, LivingEntity entity) {
        return Pony.of(entity).map(pony -> pony.getImplicitEnchantmentLevel(enchantment, initial)).orElse(initial);
    }
}
