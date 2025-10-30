package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.gui.UHud;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;

@Mixin(KeyboardInput.class)
abstract class MixinKeyboardInput extends Input {
    @Inject(method = "tick(ZF)V", at = @At("RETURN"))
    private void onTick(boolean a, float b, CallbackInfo info) {
        Pony player = Pony.of(MinecraftClient.getInstance().player);

        if (player != null) {
            if (player.getPhysics().isGravityNegative()) {
                playerInput = new PlayerInput(
                    playerInput.forward(),
                    playerInput.backward(),
                    playerInput.right(),
                    playerInput.left(),
                    playerInput.jump(),
                    playerInput.sneak(),
                    playerInput.sprint()
                );
                movementSideways = -movementSideways;
            }

            if (UHud.INSTANCE.handleInput(this)) {
                movementSideways = 0;
                movementForward = 0;
                if (playerInput.jump()) {
                    playerInput = new PlayerInput(
                        playerInput.forward(),
                        playerInput.backward(),
                        playerInput.left(),
                        playerInput.right(),
                        false,
                        playerInput.sneak(),
                        playerInput.sprint()
                    );
                }
            }
        }
    }
}
