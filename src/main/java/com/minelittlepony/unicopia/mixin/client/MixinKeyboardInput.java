package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;

@Mixin(KeyboardInput.class)
abstract class MixinKeyboardInput extends Input {
    @Inject(method = "tick(Z)V", at = @At("RETURN"))
    private void onTick(boolean a, CallbackInfo info) {
        Pony player = Pony.of(MinecraftClient.getInstance().player);

        if (player != null && player.getPhysics().isGravityNegative()) {
            boolean tmp = pressingLeft;

            pressingLeft = pressingRight;
            pressingRight = tmp;

            movementSideways = -movementSideways;

            if (player.getMaster().getAbilities().flying && !player.getPhysics().isFlying()) {
                tmp = jumping;
                jumping = sneaking;
                sneaking = tmp;
            }
        }
    }
}
