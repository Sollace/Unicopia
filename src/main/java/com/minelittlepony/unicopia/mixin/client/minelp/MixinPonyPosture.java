package com.minelittlepony.unicopia.mixin.client.minelp;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.api.pony.PonyPosture;
import com.minelittlepony.unicopia.EquinePredicates;
import net.minecraft.entity.LivingEntity;

@Mixin(PonyPosture.class)
abstract class MixinPonyPosture {
    @Inject(method = "isPartiallySubmerged", at = @At("HEAD"), cancellable = true)
    private static void isPartiallySubmerged(LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
        if (EquinePredicates.PLAYER_SEAPONY.test(entity)) {
            info.setReturnValue(true);
        }
    }
}
