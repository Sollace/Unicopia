package com.minelittlepony.unicopia.mixin.gravity;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.UnicopiaClient;

import net.minecraft.client.sound.Source;
import net.minecraft.util.math.Vec3d;

@Mixin(Source.class)
abstract class MixinSoundSource {
    @Shadow
    private @Final int pointer;

    @Nullable
    private Vec3d unicopiaOriginalPos = null;

    @Inject(method = "setPosition", at = @At("HEAD"))
    private Vec3d modifyPosition(Vec3d pos, CallbackInfo info) {
        unicopiaOriginalPos = pos;
        if (isRelative()) {
            return UnicopiaClient.getAdjustedSoundPosition(pos);
        }
        return pos;
    }

    @Inject(method = "setRelative", at = @At("RETURN"))
    private void onSetRelative(boolean relative, CallbackInfo info) {
        if (unicopiaOriginalPos != null) {
            ((Source)(Object)this).setPosition(unicopiaOriginalPos);
            unicopiaOriginalPos = null;
        }
    }

    @Unique
    private boolean isRelative() {
        return AL10.alGetSourcei(pointer, AL10.AL_SOURCE_RELATIVE) == Source.field_31894 /*(1)*/;
    }
}
