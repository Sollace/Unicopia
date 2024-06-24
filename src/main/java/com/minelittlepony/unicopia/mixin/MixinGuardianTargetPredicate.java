package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.effect.SeaponyGraceStatusEffect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;

@Mixin(targets = "net.minecraft.entity.mob.GuardianEntity$GuardianTargetPredicate")
abstract class MixinGuardianTargetPredicate {
    @Shadow
    private @Final GuardianEntity owner;

    @ModifyReturnValue(method = "test", at = @At("RETURN"))
    private boolean unicopia_excludeSeaponysGrace(boolean result, @Nullable LivingEntity target) {
        return result && SeaponyGraceStatusEffect.hasIre(target, owner);
    }
}
