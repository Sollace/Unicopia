package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.world.client.gui.UHud;

import net.minecraft.client.gui.hud.InGameHud;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Inject(method = "render(F)V", at = @At("HEAD"))
    private void onRender(float tickDelta, CallbackInfo info) {
        UHud.instance.render((InGameHud)(Object)this, tickDelta);
    }
}
