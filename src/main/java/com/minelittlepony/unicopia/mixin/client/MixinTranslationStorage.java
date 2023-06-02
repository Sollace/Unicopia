package com.minelittlepony.unicopia.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resource.language.TranslationStorage;

@Mixin(TranslationStorage.class)
abstract class MixinTranslationStorage {
    @Shadow
    private @Final Map<String, String> translations;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void onGet(String key, String fallback, CallbackInfoReturnable<String> info) {
        if (key != null && key.contains(".pegasus") && !translations.containsKey(key)) {
            info.setReturnValue(translations.getOrDefault(key.replace(".pegasus", ""), fallback));
        }
    }

    @Inject(method = "hasTranslation", at = @At("RETURN"), cancellable = true)
    public void onHasTranslation(String key, CallbackInfoReturnable<Boolean> info) {
        if (key != null
                && key.contains(".pegasus")
                && !info.getReturnValueZ()
                && translations.containsKey(key.replace(".pegasus", ""))) {
            info.setReturnValue(true);
        }
    }
}