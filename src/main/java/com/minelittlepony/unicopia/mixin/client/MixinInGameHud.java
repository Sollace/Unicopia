package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.entity.effect.RaceChangeStatusEffect;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InGameHud.class)
abstract class MixinInGameHud {
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V", at = @At("HEAD"))
    private void onRender(MatrixStack stack, float tickDelta, CallbackInfo info) {
        UHud.INSTANCE.render((InGameHud)(Object)this, stack, tickDelta);
    }
}

@Mixin(targets = "net.minecraft.client.gui.hud.InGameHud$HeartType")
abstract class MixinInGameHud$HeartType {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
        method = "fromPlayerState(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/client/gui/hud/InGameHud$HeartType;",
        at = @At("RETURN"),
        cancellable = true)
    private static void onFromPlayerState(PlayerEntity player, CallbackInfoReturnable<Enum> cbi) {
        if (RaceChangeStatusEffect.hasEffect(player)) {
            cbi.setReturnValue(Enum.valueOf(cbi.getReturnValue().getClass(), "WITHERED"));
        }
    }
}