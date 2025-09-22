package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.client.gui.HudEffects;
import com.minelittlepony.unicopia.client.gui.UHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(InGameHud.class)
abstract class MixinInGameHud {

    @Shadow
    abstract PlayerEntity getCameraPlayer();

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        HudEffects.tryApply(getCameraPlayer(), tickCounter, true);
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"))
    private void onRenderHorbat(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        UHud.INSTANCE.render((InGameHud)(Object)this, context, tickCounter);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void afterRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo info) {
        HudEffects.tryApply(getCameraPlayer(), tickCounter, false);
    }

    /*@ModifyArg(
            method = "drawHeart",
            at = @At(value = "INVOKE", target = "net/minecraft/client/gui/DrawContext.drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"),
            index = 0
    )
    private Identifier adjustHeartTexture(Identifier texture, @Local InGameHud.HeartType heartsType, @Local(ordinal = 0) boolean hardcore, @Local(ordinal = 1) boolean blinking, @Local(ordinal = 2) boolean half) {
        return UHud.getHeartTexture(heartsType, texture, hardcore, blinking, half);
    }*/
}

@Mixin(InGameHud.HeartType.class)
abstract class MixinInGameHud$HeartType {
    @ModifyReturnValue(
        method = "fromPlayerState(Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/client/gui/hud/InGameHud$HeartType;",
        at = @At("RETURN"))
    private static InGameHud.HeartType onFromPlayerState(InGameHud.HeartType heartsType, PlayerEntity player) {
        return UHud.getHeartsType(player, heartsType);
    }
}