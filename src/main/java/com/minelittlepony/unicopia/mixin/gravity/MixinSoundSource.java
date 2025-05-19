package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.client.UnicopiaClient;
import net.minecraft.client.sound.Source;
import net.minecraft.util.math.Vec3d;

@Mixin(Source.class)
abstract class MixinSoundSource {
    @ModifyVariable(method = "setPosition", at = @At("HEAD"), argsOnly = true)
    private Vec3d modifyPosition(Vec3d pos) {
        return UnicopiaClient.getAdjustedSoundPosition(pos);
    }
}
