package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.client.gui.HudEffects;
import com.minelittlepony.unicopia.client.gui.UHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InGameHud.class)
abstract class MixinInGameHud {

    @Shadow
    abstract PlayerEntity getCameraPlayer();

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;F)V", at = @At("HEAD"))
    private void onRender(DrawContext context, float tickDelta, CallbackInfo info) {
        HudEffects.tryApply(getCameraPlayer(), tickDelta, true);
        UHud.INSTANCE.render((InGameHud)(Object)this, context, tickDelta);
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;F)V", at = @At("RETURN"))
    private void afterRender(DrawContext context, float tickDelta, CallbackInfo info) {
        HudEffects.tryApply(getCameraPlayer(), tickDelta, false);
    }
}

@Mixin(InGameHud.HeartType.class)
abstract class MixinInGameHud$HeartType {
    @Inject(
        method = "fromPlayerState(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/client/gui/hud/InGameHud$HeartType;",
        at = @At("RETURN"),
        cancellable = true)
    private static void onFromPlayerState(PlayerEntity player, CallbackInfoReturnable<InGameHud.HeartType> cbi) {
        InGameHud.HeartType heartsType = UHud.getHeartsType(player);
        if (heartsType != null) {
            cbi.setReturnValue(heartsType);
        }
    }
}