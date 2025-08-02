package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellSlots;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.ClearAllEffectsConsumeEffect;
import net.minecraft.world.World;

@Mixin(ClearAllEffectsConsumeEffect.class)
abstract class MixinMilkBucketItem {
    @Inject(method = "onConsume", at = @At("HEAD"))
    private void onOnConsume(World world, ItemStack stack, LivingEntity user, CallbackInfoReturnable<Boolean> info) {
        Caster.of(user).map(Caster::getSpellSlot).ifPresent(SpellSlots::clear);
    }
}
