package com.minelittlepony.unicopia.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;

@Mixin(TargetPredicate.class)
abstract class MixinTargetPredicate {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    public void onTest(@Nullable LivingEntity baseEntity, LivingEntity targetEntity, CallbackInfoReturnable<Boolean> info) {
        Equine<?> eq = Equine.of(targetEntity);
        if (eq instanceof Pony) {
            ((Pony)eq).getSpellOrEmpty(DisguiseSpell.class).ifPresent(spell -> {
                if (spell.getDisguise().getAppearance() == baseEntity) {
                    info.setReturnValue(false);
                }
            });
        }
    }
}
